package com.iamport.sdk.domain.core

import android.app.Application
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iamport.sdk.BuildConfig.DEBUG
import com.iamport.sdk.data.sdk.*
import com.iamport.sdk.domain.di.IamportKoinContext
import com.iamport.sdk.domain.di.IamportKoinContext.koinApp
import com.iamport.sdk.domain.di.apiModule
import com.iamport.sdk.domain.di.appModule
import com.iamport.sdk.domain.di.httpClientModule
import com.iamport.sdk.domain.service.ChaiService
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.Event
import com.iamport.sdk.domain.utils.Foreground
import com.iamport.sdk.domain.utils.PreventOverlapRun
import com.iamport.sdk.presentation.activity.IamportSdk
import com.iamport.sdk.presentation.contract.WebViewActivityContract
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.BuildConfig
import com.orhanobut.logger.Logger
import com.orhanobut.logger.Logger.*
import com.orhanobut.logger.PrettyFormatStrategy
import org.koin.android.ext.koin.androidContext
import org.koin.android.logger.AndroidLogger
import org.koin.core.KoinApplication
import org.koin.core.component.KoinApiExtension
import org.koin.core.context.startKoin


object Iamport {
    private var webViewLauncher: ActivityResultLauncher<Payment>? = null // SDK Activity 열기 위한 Contract
    private var iamportSdk: IamportSdk? = null

//    private var impCallbackImpl: ICallbackPaymentResult? = null // 결제결과 callback type#1 ICallbackPaymentResult 구현

    private var impCallbackFunction: ((IamPortResponse?) -> Unit)? = null // 결제결과 callbck type#2 함수 호출
    private var approveCallback: ((IamPortApprove) -> Unit)? = null // 차이 결제 상태 approve 콜백

    private var close = MutableLiveData<Event<Unit>>() // FIXME 라이브데이터 쓸 이유가 있나? sdk?.close() 하면 되자너
    private var finish = MutableLiveData<Event<Unit>>() // FIXME 라이브데이터 쓸 이유가 있나? sdk?.finish() 하면 되자너

    private var activity: ComponentActivity? = null
    private var fragment: Fragment? = null
    private var preventOverlapRun: PreventOverlapRun? = null

    private var isCreated = false

    private fun clear() {
        fragment = null
        activity = null
        iamportSdk = null
    }

    private fun createInitialData() {
        this.close = MutableLiveData()
        this.finish = MutableLiveData()
        this.preventOverlapRun = PreventOverlapRun()
    }

    private fun iamportCreated(): Boolean {
        if (!isCreated) {
            Log.e(CONST.IAMPORT_LOG, "IAMPORT SDK was not created. Please initialize it in Application class")
        }
        return isCreated
    }

    fun getKoinApplition(): KoinApplication? {
        return koinApp
    }

    fun create(app: Application) {
        createWithKoin(app)
    }

    /**
     * Application instance 를 통해 SDK 생명주기 감지, DI 초기화
     */
    // TODO Application 사용하지 않는 방안 모색
    fun createWithKoin(app: Application, koinApp: KoinApplication? = null) {

        IamportKoinContext.koinApp = koinApp
            ?: startKoin {
                logger(AndroidLogger())
                androidContext(app)
                modules(httpClientModule, apiModule, appModule)
            }

        Foreground.init(app)

        val logBuilder = PrettyFormatStrategy.newBuilder()
        val formatStrategy: PrettyFormatStrategy = if (DEBUG) {
            logBuilder
                .methodCount(3)
                .tag(CONST.IAMPORT_LOG)
                .build()
        } else {
            logBuilder
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)         // (Optional) How many method line to show. Default 2
                .methodOffset(5)        // (Optional) Hides internal method calls up to offset. Default 5
                .tag(CONST.IAMPORT_LOG)
                .build()
        }

        addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                if (!DEBUG && priority <= Logger.DEBUG) {
                    return false
                }
                return true
            }
        })

        isCreated = true
        d("Create IAMPORT SDK")

//        v("LOG TEST VERBOSE")
//        d("LOG TEST DEBUG")
//        i("LOG TEST INFO")
//        w("LOG TEST WANRING")
//        e("LOG TEST ERROR")
    }

    /**
     * SDK Activity 열기 위한 Contract for Activity
     * @param componentActivity : Host Activity
     */
    fun init(componentActivity: ComponentActivity) {

        if (!iamportCreated()) {
            return
        }

        d("INITIALIZE IAMPORT SDK for activity")

        clear()
        createInitialData()

        webViewLauncher = componentActivity.registerForActivityResult(WebViewActivityContract()) {
            callback(it)
        }

        this.activity = componentActivity
        this.iamportSdk =
            IamportSdk(
                activity = componentActivity,
                webViewLauncher = webViewLauncher,
                close = close,
                finish = finish
            )
    }

    /**
     * SDK Activity 열기 위한 Contract for Fragment
     * @param fragment : Host Fragment
     */
    fun init(fragment: Fragment) {

        if (!iamportCreated()) {
            return
        }

        d("INITIALIZE IAMPORT SDK for fragment")

        clear()
        createInitialData()

        webViewLauncher = fragment.registerForActivityResult(WebViewActivityContract()) {
            callback(it)
        }

        this.fragment = fragment
        this.activity = fragment.activity
        this.iamportSdk = IamportSdk(
            fragment = fragment,
            webViewLauncher = webViewLauncher,
            close = close,
            finish = finish
        )
    }

    /**
     * 외부에서 차이 최종결제 요청
     */
    fun approvePayment(approve: IamPortApprove) {
        iamportSdk?.requestApprovePayments(approve)
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

    fun enableChaiPollingForegroundService(enableService: Boolean, enableFailStopButton: Boolean = false) {
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
    fun certification(
        userCode: String, iamPortCertification: IamPortCertification, resultCallback: (IamPortResponse?) -> Unit
    ) {
        preventOverlapRun?.launch {
            coreCertification(userCode, iamPortCertification, resultCallback)
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
    internal fun coreCertification(
        userCode: String,
        iamPortCertification: IamPortCertification,
        paymentResultCallback: ((IamPortResponse?) -> Unit)?
    ) {
        this.impCallbackFunction = paymentResultCallback

        iamportSdk?.initStart(Payment(userCode, iamPortCertification = iamPortCertification), paymentResultCallback)
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

        iamportSdk?.initStart(Payment(userCode, iamPortRequest = iamPortRequest), approveCallback, paymentResultCallback)
    }
}
