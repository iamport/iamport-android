package com.iamport.sdk.domain.strategy.chai

import com.iamport.sdk.data.chai.request.OS
import com.iamport.sdk.data.chai.request.PrepareRequest
import com.iamport.sdk.data.chai.response.*
import com.iamport.sdk.data.chai.response.ChaiPaymentStatus.*
import com.iamport.sdk.data.remote.ApiHelper
import com.iamport.sdk.data.remote.ChaiApi
import com.iamport.sdk.data.remote.IamportApi
import com.iamport.sdk.data.remote.ResultWrapper
import com.iamport.sdk.data.remote.ResultWrapper.*
import com.iamport.sdk.data.sdk.*
import com.iamport.sdk.domain.strategy.base.BaseStrategy
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.Event
import com.iamport.sdk.domain.utils.Foreground
import com.orhanobut.logger.Logger.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import java.util.concurrent.atomic.AtomicInteger

// TODO: 12/1/20 구조 좀 정리하기 ㅠㅠ 너무 복잡 ㅠㅠ
@KoinApiExtension
open class ChaiStrategy : BaseStrategy() {

    private val chaiApi: ChaiApi by inject() // 차이 서버 API
    private val iamportApi: IamportApi by inject() // 아임포트 서버 API

    lateinit var chaiId: String // 차이 PG 아이디
    private var prepareData: PrepareData? = null // 차이 api 에 호출하기 위한 데이터

    private val pollingDelay = CONST.POLLING_DELAY // 폴링 간격
    private var pollingId = AtomicInteger() // 폴링 중복호출 방지위한 아이디 인덱스

    // 폴링 타임아웃
    private var tryOut = false
    private var tryCount = 0

    private var networkError: String? = null

    /**
     *  SDK init
     */
    override fun init() {
        d("ChaiStrategy init")
        tryOut = false
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
     * prepareData 데이터 초기화
     */
    protected open fun clearData() {
        prepareData = null
        pollingId = AtomicInteger()
        tryCount = 0
        bus.isPolling.value = Event(false)
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

    // #4 API
    private suspend fun apiApprovePayment(
        impUserCode: String,
        impUid: String,
        paymentId: String?,
        idempotencyKey: String?,
        status: ChaiPaymentStatus,
        native: String,
    ): ResultWrapper<Approve> {
        d("try apiApprovePayment")
        return ApiHelper.safeApiCall(Dispatchers.IO) {
            iamportApi.postApprove(impUserCode, impUid, paymentId, idempotencyKey, status, native)
        }
    }

    suspend fun doWork(chaiId: String, payment: Payment) {
        this.chaiId = chaiId
        doWork(payment)
    }

    /**
     * 간략한 시퀀스 설명
     * * 1. IMP 서버에 유저 정보 요청해서 chai id 얻음 -> 결제 시퀀스 전 체크하는 것으로 수정함₩
     * 2. IMP 서버에 결제시작 요청 (+ chai id)
     * 3. chai 앱 실행
     * 4. 백그라운드 chai 서버 폴링
     * 5. if(차이폴링 approve) IMP 최종승인 요청
     */
    override suspend fun doWork(payment: Payment) {
        super.doWork(payment)
        d("doWork! $payment")
//        * 2. IMP 서버에 결제시작 요청 (+ chai id)
        when (val response = apiPostPrepare(PrepareRequest.make(chaiId, payment))) {
            is NetworkError -> failureFinish(payment, prepareData, "NetworkError ${response.error}")
            is GenericError -> failureFinish(payment, prepareData, "GenericError ${response.code} ${response.error}")

            is Success -> processPrepare(response.value)
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
     * pollingDelay 간격으로 pollingProcessStatus 호출
     */
    private suspend fun pollingProcessStatus(chaiPayment: ChaiPayment, payment: Payment, data: PrepareData, idx: Int) {
        delay(pollingDelay)
        processStatus(chaiPayment, payment, data, idx)
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
        if (tryOut) { // 타임아웃
            failureFinish(payment, prepareData, "${CONST.TRY_OUT_MIN} 분 이상 결제되지 않아 결제취소 처리합니다. 결제를 재시도 해주세요.")
            return
        }

        if (networkError != null) { // 네트워크 에러
            failureFinish(payment, prepareData, "결제 실패 NetworkError $networkError")
            return
        }

        if (bus.chaiClearVersion) {
            d("새버전이라서 폴링 안할건데? 초기화 할건데?")
            clearData()
            return
        }

        requestPollingChaiStatus()
    }

    private suspend fun updatePolling(isPolling: Boolean) = withContext(Dispatchers.Main) {
        bus.isPolling.value = Event(isPolling)
    }

    /**
     * 4.  chai 서버 결제 상태 체크
     */
    private suspend fun checkChaiStatus(idx: Int) {

        prepareData?.let { data: PrepareData ->
            if (idx != pollingId.get()) {
                d("Cancel previous checkChaiStatus polling")
                return
            }
            tryCount++
//            updatePolling(true)

            when (val response =
                apiGetChaiStatus(data.idempotencyKey.toString(), data.publicAPIKey.toString(), data.paymentId.toString())) {

                is NetworkError -> {
                    networkError = response.error
                    if (tryCount > CONST.TRY_OUT_COUNT) { // 타임아웃
                        i("결제 실패 tryOut & NetworkError ${response.error}")
                        tryOut = true
                        clearData()
                        networkError = null
                    } else {
                        tryOut = false
                        if (Foreground.isBackground || !Foreground.isScreenOn) {
                            d("NetworkError 결제 폴링! ${response.error}")
                            pollingCheckStatus(pollingDelay, idx)
                            updatePolling(true)
                        } else {
                            d("NetworkError 결제 clearData")
                            clearData()
                        }
                    }
                }

                is GenericError -> {
                    networkError = null
                    failureFinish(payment, prepareData, "GenericError ${response.code} ${response.error}")
                }

                is Success -> {
                    tryCount--
                    networkError = null
                    processStatus(response.value, payment, data, idx)
                }
            }
        } ?: run {
            d("Stop poilling, Not found PrepareData")
        }
    }

    private suspend fun confirmMerchant(payment: Payment, data: PrepareData) {
        withContext(Dispatchers.Default) {
            IamPortApprove.make(payment, data).run {
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
                return@run
            }

            when (val response =
                apiGetChaiStatus(idempotencyKey.toString(), publicAPIKey.toString(), paymentId.toString())) {
                is NetworkError -> failureFinish(payment, prepareData, "NetworkError ${response.error}")
                is GenericError -> failureFinish(payment, prepareData, "GenericError ${response.code} ${response.error}")
                is Success -> {
                    val status = ChaiPaymentStatus.from(response.value.status)
                    if (status == approved) {
                        processApprovePayments(approve)
                    } else {
                        i("최종결제 진행하지 않습니다. $status")
                        d("상세정보 ${response.value}")
                    }
                }
            }
        }
    }

    /**
     * 현재 결제중인 데이터와 머천트앱으로부터 전달받은 데이터가 동일한가 비교
     */
    private fun matchApproveData(approve: IamPortApprove): Boolean {
        return approve.run {
            payment.userCode == userCode && payment.iamPortRequest.merchant_uid == merchantUid
                    && prepareData?.paymentId == paymentId && prepareData?.impUid == impUid
                    && prepareData?.idempotencyKey == idempotencyKey && prepareData?.publicAPIKey == publicAPIKey
        }
    }

    private suspend fun processApprovePayments(approve: IamPortApprove) {
        i("결제 최종 승인 요청~~~")
        approve.run {
            when (val response =
                apiApprovePayment(
                    userCode,
                    idempotencyKey.toString(),
                    paymentId,
                    idempotencyKey,
                    approved,
                    OS.aos.name
                )) {
                is NetworkError -> failureFinish(payment, prepareData, "최종결제 요청 실패 NetworkError ${response.error}")
                is GenericError -> failureFinish(payment, prepareData, "GenericError ${response.code} ${response.error}")

                is Success -> processApprove(response.value)
            }
        }
    }

    /**
     * 차이 서버에서 데이터 가져왔을 때 처리
     */
    private suspend fun processStatus(chaiPayment: ChaiPayment, payment: Payment, data: PrepareData, idx: Int) {
        if (idx != pollingId.get()) {
            d("Cancel previous processStatus polling")
            return
        }
        tryCount++

        when (ChaiPaymentStatus.from(chaiPayment.status)) {
            approved -> confirmMerchant(payment, data)

            confirmed -> successFinish(payment, prepareData, "가맹점 측 결제 승인 완료 (결제 성공) ${chaiPayment.status}")

            partial_confirmed -> successFinish(payment, prepareData, "부분 취소된 결제 ${chaiPayment.status}")

            waiting, prepared -> {
                if (tryCount > CONST.TRY_OUT_COUNT) { // 타임아웃
                    tryOut = true
                    i("${CONST.TRY_OUT_MIN}분 이상 결제되지 않아 결제취소 처리합니다. 결제를 재시도 해주세요.")
                    clearData()
                } else {
                    tryOut = false
//                    d("Foreground.isHome ${Foreground.isHome}")
                    if (Foreground.isHome) {
                        d("홈이라서 체크 로컬 폴링 $chaiPayment")
                        updatePolling(false)
                        pollingProcessStatus(chaiPayment, payment, data, idx)
                    } else {
                        if (Foreground.isBackground && Foreground.isScreenOn) {
                            d("결제 리모트 폴링! $chaiPayment")
                            updatePolling(true)
                            pollingCheckStatus(pollingDelay, idx)
                        } else {
                            d("프로세스 체크 로컬 폴링 $chaiPayment")
                            updatePolling(false)
                            pollingProcessStatus(chaiPayment, payment, data, idx)
                        }
                    }
                }
            }

            user_canceled, canceled, failed, timeout -> failureFinish(payment, prepareData, "결제실패 ${chaiPayment.status}")

            else -> failureFinish(payment, prepareData, "결제실패 ${chaiPayment.status}")

        }
    }


    // * 3. chai 앱 실행
    private suspend fun processPrepare(prepare: Prepare) {
        withContext(Dispatchers.Main) {
            if (prepare.code == 0) {
                prepare.data.returnUrl?.let {
                    bus.chaiUri.value = Event(it)
                }
                this@ChaiStrategy.prepareData = prepare.data
            } else {
                w(prepare.msg)
                failureFinish(payment, prepareData, prepare.msg)
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
