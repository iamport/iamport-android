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
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.di.provideChaiApi
import com.iamport.sdk.domain.strategy.base.BaseStrategy
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.Event
import com.orhanobut.logger.Logger.*
import kotlinx.coroutines.*
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.util.concurrent.atomic.AtomicInteger

open class ChaiStrategy : BaseStrategy() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val iamportApi: IamportApi by inject() // 아임포트 서버 API
    private lateinit var chaiApi: ChaiApi // 차이 서버 API

    private lateinit var chaiId: String // 차이 PG 아이디

    private var prepareData: PrepareData? = null // 차이 api 에 호출하기 위한 데이터

    private var timeOutTime: Long = 0 // 차이 결제 타임 아웃이 동작할 타임

    private val pollingId = AtomicInteger() // 폴링 아이디로 이전 폴링을 클렌징 하기 위해 사용

    /**
     *  SDK init
     *  => doWork 때 동작
     */
    override fun init() {
        d("ChaiStrategy init")
        clearData() // init
    }

    /**
     * SDK 종료시 처리
     */
    override fun sdkFinish(response: IamPortResponse?) {
        clearData() // finish
        Iamport.callback(response) // 팝업모드에서 종료 호출하기 위함
    }

    /**
     * prepareData 데이터 초기화
     */
    protected open fun clearData() {
        d("clearData ChaiStrategy")
        prepareData = null
        timeOutTime = 0
        bus.isPolling.value = Event(false)
    }


    // 타임아웃 상황
    private fun isTimeOut(): Boolean {
        d("now ${System.currentTimeMillis()} VS timeOutTime $timeOutTime")
        return System.currentTimeMillis() >= timeOutTime
    }

    suspend fun doWork(chaiId: String, payment: Payment) {
        scope.coroutineContext.cancelChildren() // 차이 데이터 타임아웃 초기화 코루틴 cancel
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
        super.doWork(payment) // + init 까지 함

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
        d("try apiGetChaiStatusSubscription")
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


    // 현재 pollingId 로 딱 리모트 상태 한번 체크
    suspend fun onceCheckRemoteChaiStatus() {
        checkRemoteChaiStatus(pollingId.get(), doPolling = false)
    }

    /**
     * 4.  chai 서버 결제 상태 체크
     */
    private suspend fun checkRemoteChaiStatus(currentId: Int, doPolling: Boolean = true) {

        prepareData?.let { prepareData: PrepareData ->

            isPolling(true)

            when (val response = requestGetChaiStatus(prepareData)) {
                is NetworkError -> {
                    i("네트워크 통신실패로 인한 폴링 시도!! ${response.error}")
                    tryPolling(currentId)
                }
                is GenericError -> {
                    failureFinish(payment, this.prepareData, "GenericError ${response.code} ${response.error}")
                }

                is Success -> {

                    val displayStatus = when (val result = response.value) {
                        is ChaiPayment -> result.displayStatus
                        is ChaiPaymentSubscription -> result.displayStatus
                        else -> CONST.EMPTY_STR
                    }

                    val impUid = when (val result = response.value) {
                        is ChaiPayment -> result.idempotencyKey
                        is ChaiPaymentSubscription -> result.idempotencyKey
                        else -> CONST.EMPTY_STR
                    }

                    processStatus(displayStatus, payment, prepareData, impUid, doPolling, currentId)
                }
            }

        } ?: run {
            d("prepareData 정보 찾을 수 없으므로 동작하지 않음")
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
            scope.launch {
                delay(CONST.CHAI_FINAL_PAYMENT_TIME_OUT_SEC)
                i("차이 데이터 초기화! [${CONST.CHAI_FINAL_PAYMENT_TIME_OUT_SEC}]ms")
                clearData() // 최종 결제 타임아웃으로 인한 초기화
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
        return prepareData?.let { prepareData ->
            approve.run {
                payment.userCode == userCode
                        && payment.getMerchantUid() == merchantUid
                        && payment.getCustomerUid() == customerUid
                        && prepareData.paymentId == paymentId
                        && prepareData.subscriptionId == subscriptionId
                        && prepareData.impUid == impUid
                        && prepareData.idempotencyKey == idempotencyKey
                        && prepareData.publicAPIKey == publicAPIKey
            }
        } ?: run { false }
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

    private suspend fun tryPolling(currentId: Int, pollingDelay: Long = CONST.POLLING_DELAY) {
        i("폴링!!")

        if (isTimeOut()) { // 타임아웃
            if (prepareData == null) {
                d("isTimeOut 이나, payment : ${payment}, prepareData : ${prepareData}")
                clearData() // timeout && prepareData == null
                return
            }

            val msg = "[${CONST.TIME_OUT_MIN}] 분 이상 결제되지 않아 미결제 처리합니다. 결제를 재시도 해주세요."
            i(msg)
            clearData() // timeout
//            failureFinish(payment, this.prepareData, msg)
            return
        }
        if (currentId < pollingId.get()) {
            d("[이전 폴링 클렌징, 지울 ID : $currentId, 최신 ID : $pollingId]")
            return
        }

        d("폴링 동작 currentId($currentId)")
        delay(pollingDelay)
        checkRemoteChaiStatus(currentId)
    }

    /**
     * 차이 서버에서 데이터 가져왔을 때 처리
     */
    private suspend fun processStatus(
        displayStatus: String,
        payment: Payment,
        prepareData: PrepareData,
        impUid: String,
        doPolling: Boolean = true,
        currentId: Int,
    ) {

        when (val status = ChaiPaymentStatus.from(displayStatus)) {
            approved -> confirmMerchant(payment, prepareData, status)

            confirmed -> successFinish(payment.getMerchantUid(), impUid, "가맹점 측 결제 승인 완료 (결제 성공) [${displayStatus}]")

            partial_confirmed -> successFinish(payment.getMerchantUid(), impUid, "부분 취소된 결제 [${displayStatus}]")

            waiting, prepared -> {
                if (!doPolling) {
                    d("this period is just check, not remote polling.")
                    return
                }
                tryPolling(currentId)
            }

            user_canceled, canceled, failed, timeout, inactive, churn -> {
                d("결제실패 [${displayStatus}]")
                IamPortApprove.make(payment, prepareData, status).run {
                    requestApproveToIamport(this)
                }
            }

            else -> failureFinish(payment.getMerchantUid(), impUid, "결제실패 [${displayStatus}]")
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

                if (code != 0) {
                    w(msg)
                    failureFinish(payment, prepareData, msg)
                    return@withContext
                }

                data.run {

                    if (subscriptionId.isNullOrBlank() && paymentId.isNullOrBlank()) {
                        val errMsg = "subscriptionId & paymentId 모두 값이 없습니다."
                        w(errMsg)
                        failureFinish(payment, prepareData, errMsg)
                    }

                    prepareData = this
                    bus.chaiUri.value = Event(returnUrl)

                    timeOutTime = System.currentTimeMillis() + CONST.TIME_OUT
                    d("set timeOutTime $timeOutTime")

                    chaiApi = provideChaiApi(
                        CHAI_MODE.getChaiUrl(mode),
                        get(named("${CONST.KOIN_KEY}Gson")),
                        get(named("${CONST.KOIN_KEY}provideOkHttpClient"))
                    ) // mode 에 따라 chaiApi 생성

                    // 최초 polling 시작점 pollingId 업데이트 하고 시작(이전 폴링 동작은 클렌징 됨)
                    tryPolling(pollingId.incrementAndGet(), 0)
                }
            }
        }
    }


    /**
     * 최종 결제 처리
     */
    protected open suspend fun processApprove(approve: Approve) {
        if (approve.code == 0) {
            successFinish(approve.data.merchantUid, approve.data.impUid, "결제 성공")
        } else {
            failureFinish(approve.data.merchantUid, approve.data.impUid, approve.msg)
        }
    }

    /**
     * 외부에 폴링여부 알리기 위해 사용
     */
    private suspend fun isPolling(isPolling: Boolean) = withContext(Dispatchers.Main) {
        bus.isPolling.value = Event(isPolling)
    }


}
