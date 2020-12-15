package com.iamport.sdk.domain.core

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iamport.sdk.data.sdk.IamPortApprove
import com.iamport.sdk.data.sdk.IamPortRequest
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.service.ChaiService
import com.iamport.sdk.domain.utils.PreventOverlapRun
import com.iamport.sdk.domain.utils.Event
import com.iamport.sdk.domain.utils.Foreground
import com.iamport.sdk.presentation.activity.IamportSdk
import com.iamport.sdk.presentation.contract.WebViewActivityContract
import com.orhanobut.logger.Logger.d
import org.koin.core.component.KoinApiExtension


object Iamport {
    private var webViewLauncher: ActivityResultLauncher<Payment>? = null // SDK Activity 열기 위한 Contract
    private var iamportSdk: IamportSdk? = null

//    private var impCallbackImpl: ICallbackPaymentResult? = null // 결제결과 callback type#1 ICallbackPaymentResult 구현

    private var impCallbackFunction: ((IamPortResponse?) -> Unit)? = null // 결제결과 callbck type#2 함수 호출
    private var approveCallback: ((IamPortApprove) -> Unit)? = null // 차이 결제 상태 approve 콜백

    private var approvePayment = MutableLiveData<Event<IamPortApprove>>()
    private var close = MutableLiveData<Event<Unit>>()
    private var finish = MutableLiveData<Event<Unit>>()

    var activity: ComponentActivity? = null
    private var fragment: Fragment? = null
    private var preventOverlapRun: PreventOverlapRun? = null

    private fun clear() {
        fragment = null
        activity = null
        iamportSdk = null
    }

    private fun createLiveData() {
        this.approvePayment = MutableLiveData()
        this.close = MutableLiveData()
        this.finish = MutableLiveData()
    }

    /**
     * SDK Activity 열기 위한 Contract for Activity
     * @param componentActivity : Host Activity
     */
    fun init(componentActivity: ComponentActivity) {
        d("init")
        clear()
        createLiveData()

        webViewLauncher = componentActivity.registerForActivityResult(WebViewActivityContract()) {
            callback(it)
        }
        this.activity = componentActivity
        this.iamportSdk =
            IamportSdk(
                activity = componentActivity,
                webViewLauncher = webViewLauncher,
                approvePayment = approvePayment,
                close = close,
                finish = finish
            )
        this.preventOverlapRun = PreventOverlapRun()
    }

    /**
     * SDK Activity 열기 위한 Contract for Fragment
     * @param fragment : Host Fragment
     */
    fun init(fragment: Fragment) {
        d("init")
        clear()
        createLiveData()

        webViewLauncher = fragment.registerForActivityResult(WebViewActivityContract()) {
            callback(it)
        }

        this.fragment = fragment
        this.activity = fragment.activity
        this.iamportSdk = IamportSdk(
            fragment = fragment,
            webViewLauncher = webViewLauncher,
            approvePayment = approvePayment,
            close = close,
            finish = finish
        )
        this.preventOverlapRun = PreventOverlapRun()
    }

    /**
     * 외부에서 차이 최종결제 요청
     */
    fun chaiPayment(approve: IamPortApprove) {
        approvePayment.postValue(Event(approve))
    }

    /**
     * 외부에서 SDK 종료
     */
    @MainThread
    fun close() {
        close.value = (Event(Unit))
    }

    /**
     * 외부에서 SDK 실패 종료
     */
    @MainThread
    fun failFinish() {
        finish.value = (Event(Unit))
    }

    fun enableChaiPollingForegroundService(enableService: Boolean, enableFailStopButton: Boolean = true) {
        ChaiService.enableForegroundService = enableService
        ChaiService.enableForegroundServiceStopButton = enableFailStopButton
    }

    fun isPolling(): LiveData<Event<Boolean>>? {
        return iamportSdk?.isPolling()
    }

    fun isPollingValue(): Boolean {
        return isPolling()?.value?.peekContent() ?: false
    }

    private val callback = fun(iamPortResponse: IamPortResponse?) {
//        impCallbackImpl?.result(iamPortResponse)
        impCallbackFunction?.invoke(iamPortResponse)
    }

    /**
     * 결제 요청
     * @param ((IamPortApprove?) -> Unit)? : (옵셔널) 차이 최종 결제 요청전 콜백
     * @param ICallbackPaymentResult? : 결제결과 callback type#1 ICallbackPaymentResult 구현
     */
    fun payment(
        userCode: String, iamPortRequest: IamPortRequest, approveCallback: ((IamPortApprove) -> Unit)? = null, paymentResultCallback: ICallbackPaymentResult?,
    ) {
        preventOverlapRun?.launch {
            corePayment(userCode, iamPortRequest, approveCallback) { paymentResultCallback?.result(it) }
        }
    }

    /**
     * 결제 요청
     * @param ((IamPortApprove?) -> Unit)? : (옵셔널) 차이 최종 결제 요청전 콜백
     * @param (IamPortResponse?) -> Unit: ICallbackPaymentResult? : 결제결과 callbck type#2 함수 호출
     */
    fun payment(
        userCode: String, iamPortRequest: IamPortRequest, approveCallback: ((IamPortApprove) -> Unit)? = null, paymentResultCallback: (IamPortResponse?) -> Unit
    ) {
        preventOverlapRun?.launch {
            corePayment(userCode, iamPortRequest, approveCallback, paymentResultCallback)
        }
    }

    @KoinApiExtension
    internal fun corePayment(
        userCode: String,
        iamPortRequest: IamPortRequest,
        approveCallback: ((IamPortApprove) -> Unit)?,
        paymentResultCallback: ((IamPortResponse?) -> Unit)?
    ) {
        this.approveCallback = approveCallback
        this.impCallbackFunction = paymentResultCallback

        iamportSdk?.initStart(Payment(userCode, iamPortRequest), approveCallback, paymentResultCallback)
    }
}
