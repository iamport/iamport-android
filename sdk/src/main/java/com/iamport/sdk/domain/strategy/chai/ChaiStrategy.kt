package com.iamport.sdk.domain.strategy.chai

import com.iamport.sdk.data.chai.CHAI_MODE
import com.iamport.sdk.data.chai.request.OS
import com.iamport.sdk.data.chai.request.PrepareRequest
import com.iamport.sdk.data.chai.response.*
import com.iamport.sdk.data.chai.response.ChaiPaymentStatus.*
import com.iamport.sdk.data.remote.ApiHelper
import com.iamport.sdk.data.remote.ChaiApi
import com.iamport.sdk.data.remote.IamportApi
import com.iamport.sdk.data.remote.ResultWrapper
import com.iamport.sdk.data.remote.ResultWrapper.*
import com.iamport.sdk.data.sdk.IamPortApprove
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.di.provideChaiApi
import com.iamport.sdk.domain.strategy.base.BaseStrategy
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.Event
import com.iamport.sdk.domain.utils.Foreground
import com.orhanobut.logger.Logger.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.util.concurrent.atomic.AtomicInteger

// TODO: 12/1/20 구조 좀 정리하기 ㅠㅠ 너무 복잡 ㅠㅠ
@KoinApiExtension
open class ChaiStrategy : BaseStrategy() {

    private val iamportApi: IamportApi by inject() // 아임포트 서버 API
    private lateinit var chaiApi: ChaiApi // 차이 서버 API

    private lateinit var chaiId: String // 차이 PG 아이디

    private var prepareData: PrepareData? = null // 차이 api 에 호출하기 위한 데이터

    private val pollingDelay = CONST.POLLING_DELAY // 폴링 간격
    private var pollingId = AtomicInteger() // 폴링 중복호출 방지위한 아이디 인덱스

    private var tryCount = 0     // 폴링 타임아웃
    private var clearStopPolling = false  // 클리어버전이므로 종료시 폴링 안함

    private var networkError: String? = null // 네트워크 에러

    /**
     *  SDK init
     */
    override fun init() {
        d("ChaiStrategy init")
        networkError = null
        clearData()
    }

    /**
     * SDK 종료시 처리
     */
    override fun sdkFinish(response: IamPortResponse?) {
        init()
        super.sdkFinish(response)
    }

    /**
     * 외부에서 실패 종료 하기 위해 사용
     */
    fun failFinish(errMsg: String) {
        failureFinish(payment, prepareData, errMsg)
    }

    /**
     * prepareData 데이터 초기화
     */
    protected open fun clearData() {
        prepareData = null
        pollingId = AtomicInteger()
        tryCount = 0
        clearStopPolling = false
        bus.isPolling.value = Event(false)
    }

    private fun increaseTryCount() {
        tryCount++
    }

    private fun decreaseTryCount() {
        tryCount--
    }

    /**
     * 외부에 폴링여부 알리기 위해 사용
     */
    private suspend fun updatePolling(isPolling: Boolean) = withContext(Dispatchers.Main) {
        bus.isPolling.value = Event(isPolling)
    }

    // 타임아웃 상황
    private fun isTryOut(): Boolean {
        return tryCount > CONST.TRY_OUT_COUNT
    }

    // 백그라운드 또는 스크린 오프
    private fun isBgOrScreenOff(): Boolean {
        return Foreground.isBackground || !Foreground.isScreenOn
    }

    // 백그라운드이면서 스크린 온
    private fun isBgAndScreenOn(): Boolean {
        return Foreground.isBackground && Foreground.isScreenOn
    }

    suspend fun doWork(chaiId: String, payment: Payment) {
        this.chaiId = chaiId
        doWork(payment)
    }

    /**
     * 간략한 시퀀스 설명
     * 1. IMP 서버에 유저 정보 요청해서 chai id 얻음 -> 결제 시퀀스 전 체크하는 것으로 수정함
     * 2. IMP 서버에 결제시작 요청 (+ chai id)
     * 3. chai 앱 실행
     * 4. 백그라운드 chai 서버 폴링
     * 5. if(차이폴링 approve) IMP 최종승인 요청
     */
    override suspend fun doWork(payment: Payment) {
        super.doWork(payment)
        d("doWork! $payment")
//        * 2. IMP 서버에 결제시작 요청 (+ chai id)
        PrepareRequest.make(chaiId, payment)?.let {
            when (val response = apiPostPrepare(it)) {
                is NetworkError -> failureFinish(payment, prepareData, "NetworkError ${response.error}")
                is GenericError -> failureFinish(payment, prepareData, "GenericError ${response.code} ${response.error}")

                is Success -> processPrepare(response.value)
            }
        } ?: run {
            failureFinish(payment, prepareData, "cannot make PrepareRequest")
        }
    }

    // #2 API
    private suspend fun apiPostPrepare(request: PrepareRequest): ResultWrapper<Prepare> {
        d("try apiPostPrepare")
        return ApiHelper.safeApiCall(Dispatchers.IO) { iamportApi.postPrepare(request) }
    }

    // #3 API
    private suspend fun apiGetChaiStatus(idempotencyKey: String, publicApiKey: String, chaiPaymentId: String): ResultWrapper<ChaiPayment> {
        d("try apiGetChaiStatus")
        return ApiHelper.safeApiCall(Dispatchers.IO) {
            chaiApi.getChaiPayment(idempotencyKey, publicApiKey, chaiPaymentId)
        }
    }

    // #3-1 API
    private suspend fun apiGetChaiStatusSubscription(
        idempotencyKey: String,
        publicApiKey: String,
        chaiSubscriptionId: String
    ): ResultWrapper<ChaiPaymentSubscription> {
        d("try apiGetChaiStatus")
        return ApiHelper.safeApiCall(Dispatchers.IO) {
            chaiApi.getChaiPaymentSubscription(idempotencyKey, publicApiKey, chaiSubscriptionId)
        }
    }

    // #4 API
    private suspend fun apiApprovePayment(
        impUserCode: String,
        impUid: String,
        paymentId: String,
        idempotencyKey: String,
        status: ChaiPaymentStatus,
        native: String,
    ): ResultWrapper<Approve> {
        d("try apiApprovePayment")
        return ApiHelper.safeApiCall(Dispatchers.IO) {
            iamportApi.postApprove(impUserCode, impUid, paymentId, idempotencyKey, status, native)
        }
    }

    // #4-1 API 차이 정기결제
    private suspend fun apiApprovePaymentSubscription(
        impUserCode: String,
        impUid: String,
        impCustomerUid: String,
        subscriptionId: String,
        idempotencyKey: String,
        status: ChaiPaymentStatus,
        native: String,
    ): ResultWrapper<Approve> {
        d("try apiApprovePayment")
        return ApiHelper.safeApiCall(Dispatchers.IO) {
            iamportApi.postApproveSubscription(impUserCode, impUid, impCustomerUid, subscriptionId, idempotencyKey, status, native)
        }
    }


    /**
     * pollingDelay 간격으로 checkChaiStatus 호출
     */
    private suspend fun pollingCheckStatus(delayMs: Long = pollingDelay, idx: Int) {
        delay(delayMs)
        checkChaiStatus(idx)
    }

    /**
     * pollingDelay 간격으로 pollingProcessStatus 호출 (로컬 폴링 전용)
     */
    private suspend fun pollingProcessStatus(displayStatus: String, payment: Payment, data: PrepareData, idx: Int) {
        delay(pollingDelay)
        processStatus(displayStatus, payment, data, idx)
    }

    /**
     * 외부에서 폴링 시도
     */
    suspend fun requestPollingChaiStatus() {
        checkChaiStatus(pollingId.incrementAndGet())
    }

    /**
     * 차이앱 꺼졌을 때 처리
     */
    suspend fun requestCheckChaiStatus() {
        if (isTryOut()) { // 타임아웃
            failureFinish(payment, prepareData, "[${CONST.TRY_OUT_MIN}] 분 이상 결제되지 않아 결제취소 처리합니다. 결제를 재시도 해주세요.")
            return
        }

        if (networkError != null) { // 네트워크 에러
            failureFinish(payment, prepareData, "결제 실패 NetworkError $networkError")
            return
        }

        if (bus.chaiClearVersion) {
            d("차이 싱글 액티비티이므로 종료시 폴링하지 않음")
//            clearData()
            // 12/22/20 user_cancled 같은 상태도 체크해야 해서 클리어 하면 안됨
            clearStopPolling = true
            return
        }

        requestPollingChaiStatus()
    }

    private suspend fun processChaiStatusNetworkError(idx: Int, error: String?) {
        networkError = error

        when (isTryOut()) {
            true -> {
                i("결제 실패 tryOut & NetworkError $networkError")
                clearData()
                networkError = null
            }
            false -> {
                when (isBgOrScreenOff()) {
                    true -> {
                        d("NetworkError 결제 폴링! $networkError")
                        pollingCheckStatus(pollingDelay, idx)
                    }
                    false -> {
                        d("NetworkError 결제 clearData")
                        clearData()
                    }
                }
            }
        }
    }

    /**
     * 4.  chai 서버 결제 상태 체크
     */
    private suspend fun checkChaiStatus(idx: Int) {

        prepareData?.let { prepareData: PrepareData ->
            if (idx != pollingId.get()) {
                d("Cancel previous checkChaiStatus polling")
                return
            }

            increaseTryCount()
            updatePolling(true)

            when (val response = requestGetChaiStatus(prepareData)) {
                is NetworkError -> processChaiStatusNetworkError(idx, response.error)
                is GenericError -> {
                    networkError = null
                    failureFinish(payment, this.prepareData, "GenericError ${response.code} ${response.error}")
                }

                is Success -> {
                    decreaseTryCount()
                    networkError = null

                    val displayStatus = when (val result = response.value) {
                        is ChaiPayment -> result.displayStatus
                        is ChaiPaymentSubscription -> result.displayStatus
                        else -> CONST.EMPTY_STR
                    }

                    processStatus(displayStatus, payment, prepareData, idx)
                }
            }

        } ?: run {
            d("Ignore poilling, Not found PrepareData")
        }
    }


    private suspend fun requestGetChaiStatus(prepareData: PrepareData): ResultWrapper<BaseChaiPayment> {

        if (isSubscription(prepareData)) {
            return apiGetChaiStatusSubscription(prepareData.idempotencyKey, prepareData.publicAPIKey, prepareData.subscriptionId.toString())
        }

        return apiGetChaiStatus(prepareData.idempotencyKey, prepareData.publicAPIKey, prepareData.paymentId.toString())
    }


    private suspend fun confirmMerchant(payment: Payment, data: PrepareData, status: ChaiPaymentStatus) {
        withContext(Dispatchers.Default) {
            IamPortApprove.make(payment, data, status).run {
                bus.chaiApprove.postValue(Event(this))
            }

            delay(CONST.CHAI_FINAL_PAYMENT_TIME_OUT_SEC)
            withContext(Dispatchers.Main) {
                i("최종 결제 타임아웃! ${CONST.CHAI_FINAL_PAYMENT_TIME_OUT_SEC}")
                init()
            }
        }
    }

    /**
     * * 5. if(내앱 포그라운드 && 차이폴링 인증완료) IMP 최종승인 요청
     */
    suspend fun requestApprovePayments(approve: IamPortApprove) {
        d("결제 최종 승인 요청전 한번 더 상태체크")

        approve.run {
            if (!matchApproveData(this)) {
                i("결제 데이터 매칭 실패로 최종결제하지 않습니다.")
                d("상세정보\n payment :: $payment \n prepareData :: $prepareData \n approve :: $approve")
                return
            }

            // TODO approved 한번 더 체크하는거 필요할까?
//            when (val response =
//                apiGetChaiStatus(idempotencyKey, publicAPIKey, paymentId.toString())) {
//                is NetworkError -> failureFinish(payment, prepareData, "NetworkError ${response.error}")
//                is GenericError -> failureFinish(payment, prepareData, "GenericError ${response.code} ${response.error}")
//                is Success -> {
//                    val status = ChaiPaymentStatus.from(response.value.status)
//                    if (status == approved) {
//                        requestApproveToIamport(approve)
//                    } else {
//                        i("최종결제 진행하지 않습니다. $status")
//                        d("상세정보 ${response.value}")
//                    }
//                }
//            }

            requestApproveToIamport(approve)
        }
    }

    private suspend fun requestApproveToIamport(approve: IamPortApprove) {

        if (isSubscription(approve)) {
            processApprovePaymentsSubscription(approve) // 정기결제
            return
        }

        processApprovePayments(approve) // 일반결제
    }

    /**
     * 현재 결제중인 데이터와 머천트앱으로부터 전달받은 데이터가 동일한가 비교
     */
    private fun matchApproveData(approve: IamPortApprove): Boolean {
        return approve.run {
            payment.userCode == userCode
                    && payment.getMerchantUid() == merchantUid
                    && payment.getCustomerUid() == customerUid
                    && prepareData?.paymentId == paymentId
                    && prepareData?.subscriptionId == subscriptionId
                    && prepareData?.impUid == impUid
                    && prepareData?.idempotencyKey == idempotencyKey
                    && prepareData?.publicAPIKey == publicAPIKey
        }
    }

    private suspend fun processApprovePayments(approve: IamPortApprove) {
        d("결제 최종 승인 요청~~~")
        approve.run {

            if (paymentId.isNullOrBlank()) {
                failureFinish(payment, prepareData, "최종결제 요청 실패 paymentId is [$paymentId]")
                return
            }

            if (idempotencyKey.isBlank()) {
                failureFinish(payment, prepareData, "최종결제 요청 실패 idempotencyKey is [$idempotencyKey]")
                return
            }

            when (val response =
                apiApprovePayment(userCode, idempotencyKey, paymentId, idempotencyKey, approve.status, OS.aos.name)) {
                is NetworkError -> failureFinish(payment, prepareData, "최종결제 요청 실패 NetworkError [${response.error}]")
                is GenericError -> failureFinish(payment, prepareData, "최종결제 요청 실패 GenericError [${response.code}, ${response.error}]")

                is Success -> processApprove(response.value)
            }
        }
    }

    private suspend fun processApprovePaymentsSubscription(approve: IamPortApprove) {
        d("정기 결제 최종 승인 요청~~~")
        approve.run {

            if (customerUid.isNullOrBlank()) {
                failureFinish(payment, prepareData, "최종 정기결제 요청 실패 customerUid is [$customerUid]")
                return
            }

            if (subscriptionId.isNullOrBlank()) {
                failureFinish(payment, prepareData, "최종 정기결제 요청 실패 subscriptionId is [$subscriptionId]")
                return
            }

            if (idempotencyKey.isBlank()) {
                failureFinish(payment, prepareData, "최종 정기결제 요청 실패 idempotencyKey is [$idempotencyKey]")
                return
            }

            when (val response =
                apiApprovePaymentSubscription(userCode, idempotencyKey, customerUid, subscriptionId, idempotencyKey, approve.status, OS.aos.name)) {
                is NetworkError -> failureFinish(payment, prepareData, "최종 정기결제 요청 실패 NetworkError [${response.error}]")
                is GenericError -> failureFinish(payment, prepareData, "최종 정기결제 GenericError [${response.code}, ${response.error}]")

                is Success -> processApprove(response.value)
            }
        }
    }

    /**
     * 차이 서버에서 데이터 가져왔을 때 처리
     */
    private suspend fun processStatus(displayStatus: String, payment: Payment, prepareData: PrepareData, idx: Int) {
        if (idx != pollingId.get()) {
            d("Cancel previous processStatus polling")
            return
        }
        increaseTryCount()

        when (val status = ChaiPaymentStatus.from(displayStatus)) {
            approved -> confirmMerchant(payment, prepareData, status)

            confirmed -> successFinish(payment, this.prepareData, "가맹점 측 결제 승인 완료 (결제 성공) [${displayStatus}]")

            partial_confirmed -> successFinish(payment, this.prepareData, "부분 취소된 결제 [${displayStatus}]")

            waiting, prepared -> {
                if (isTryOut()) { // 타임아웃
                    val msg = "[${CONST.TRY_OUT_MIN}] 분 이상 결제되지 않아 미결제 처리합니다. 결제를 재시도 해주세요."
//                    clearData()
                    failureFinish(payment, this.prepareData, msg)
                } else if (clearStopPolling) { // 타임아웃
                    d("클리어 앱 버전이므로 폴링 취소")
                    clearData()
                } else {
                    if (isBgAndScreenOn()) {
                        d("리모트 결제 폴링! ($tryCount / ${CONST.TRY_OUT_COUNT}) => displayStatus => [${displayStatus}], prepareData => [${prepareData}]")
                        pollingCheckStatus(pollingDelay, idx)
                    } else {
                        d("로컬 결제 폴링! ($tryCount / ${CONST.TRY_OUT_COUNT}) => displayStatus => [${displayStatus}], prepareData => [${prepareData}]")
                        pollingProcessStatus(displayStatus, payment, prepareData, idx)
                    }
                }
            }

            user_canceled, canceled, failed, timeout, inactive, churn -> {
//                failureFinish(payment, this.prepareData, "결제실패 [${displayStatus}]")
                IamPortApprove.make(payment, prepareData, status).run {
                    requestApproveToIamport(this)
                }
            }

            else -> failureFinish(payment, this.prepareData, "결제실패 [${displayStatus}]")
        }
    }


    private fun isSubscription(prepareData: PrepareData): Boolean {
        return !prepareData.subscriptionId.isNullOrBlank()
    }

    private fun isSubscription(approve: IamPortApprove): Boolean {
        return !approve.subscriptionId.isNullOrBlank()
    }

    // * 3. chai 앱 실행
    private suspend fun processPrepare(prepare: Prepare) {
        withContext(Dispatchers.Main) {
            prepare.run {
                if (code == 0) {
                    data.run {

                        if (subscriptionId.isNullOrBlank() && paymentId.isNullOrBlank()) {
                            val errMsg = "subscriptionId & paymentId 모두 값이 없습니다."
                            w(errMsg)
                            failureFinish(payment, prepareData, errMsg)
                        }

                        prepareData = this
                        bus.chaiUri.value = Event(returnUrl)

                        chaiApi = provideChaiApi(
                            CHAI_MODE.getChaiUrl(mode),
                            get(named("${CONST.KOIN_KEY}Gson")),
                            get(named("${CONST.KOIN_KEY}provideOkHttpClient"))
                        ) // mode 에 따라 chaiApi 생성

                    }
                } else {
                    w(msg)
                    failureFinish(payment, prepareData, msg)
                }
            }
        }
    }


    /**
     * 최종 결제 처리
     */
    protected open suspend fun processApprove(Approve: Approve) {
        if (Approve.code == 0) {
            successFinish(payment, prepareData, "결제 성공")
        } else {
            failureFinish(payment, prepareData, Approve.msg)
        }
    }


}
