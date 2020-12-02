package com.iamport.sdk.domain.sdk

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iamport.sdk.data.sdk.IamPortRequest
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.utils.DelayRun
import com.iamport.sdk.domain.utils.Event
import com.iamport.sdk.domain.utils.SingleLiveEvent
import com.iamport.sdk.presentation.activity.IamportSdk
import com.iamport.sdk.presentation.contract.WebViewActivityContract
import com.orhanobut.logger.Logger.d
import org.koin.core.component.KoinApiExtension


object Iamport {
    private var webViewLauncher: ActivityResultLauncher<Payment>? = null // SDK Activity 열기 위한 Contract
    private var iamportSdk: IamportSdk? = null

//    private var impCallbackImpl: ICallbackPaymentResult? = null // 결제결과 callback type#1 ICallbackPaymentResult 구현

    private var impCallbackFunction: ((IamPortResponse?) -> Unit)? = null // 결제결과 callbck type#2 함수 호출

    private lateinit var iamPortRequest: IamPortRequest

    private var close = SingleLiveEvent<Unit>()

    private var activity: ComponentActivity? = null
    private var fragment: Fragment? = null
    private var delayRun: DelayRun? = null


    private fun clear() {
        fragment = null
        activity = null
        iamportSdk = null
    }


    /**
     * SDK Activity 열기 위한 Contract for Activity
     * @param componentActivity : Host Activity
     */
    fun init(componentActivity: ComponentActivity) {
        d("init")
        clear()
        webViewLauncher = componentActivity.registerForActivityResult(WebViewActivityContract()) {
            callback(it)
        }

        close = SingleLiveEvent()
        activity = componentActivity
        iamportSdk = IamportSdk(activity = componentActivity, webViewLauncher = webViewLauncher, close = close)
        delayRun = DelayRun()
    }

    /**
     * SDK Activity 열기 위한 Contract for Fragment
     * @param fragment : Host Fragment
     */
    fun init(fragment: Fragment) {
        d("init")
        clear()
        webViewLauncher = fragment.registerForActivityResult(WebViewActivityContract()) {
            callback(it)
        }

        close = SingleLiveEvent()
        this.fragment = fragment
        iamportSdk = IamportSdk(fragment = fragment, webViewLauncher = webViewLauncher, close = close)
        delayRun = DelayRun()
    }

    /**
     * 외부에서 SDK 종료
     */
    fun close() {
        close.call()
    }

    fun isPolling(): LiveData<Event<Boolean>>? {
        return iamportSdk?.isPolling()
    }

    private val callback = fun(iamPortResponse: IamPortResponse?) {
//        impCallbackImpl?.result(iamPortResponse)
        impCallbackFunction?.invoke(iamPortResponse)
    }

    /**
     * 결제 요청
     * @param ICallbackPaymentResult? : 결제결과 callback type#1 ICallbackPaymentResult 구현
     */
    fun payment(
        userCode: String, iamPortRequest: IamPortRequest, callback: ICallbackPaymentResult?,
    ) {
        delayRun?.launch {
            corePayment(userCode, iamPortRequest) { callback?.result(it) }
        }
    }

    /**
     * 결제 요청
     * @param (IamPortResponse?) -> Unit: ICallbackPaymentResult? : 결제결과 callbck type#2 함수 호출
     */
    fun payment(
        userCode: String, iamPortRequest: IamPortRequest, callback: (IamPortResponse?) -> Unit
    ) {
        delayRun?.launch {
            corePayment(userCode, iamPortRequest, callback)
        }
    }

    @KoinApiExtension
    internal fun corePayment(userCode: String, iamPortRequest: IamPortRequest, callback: ((IamPortResponse?) -> Unit)?) {
        impCallbackFunction = callback
        this.iamPortRequest = iamPortRequest

        iamportSdk?.initStart(Payment(userCode, iamPortRequest), impCallbackFunction)
    }
}
