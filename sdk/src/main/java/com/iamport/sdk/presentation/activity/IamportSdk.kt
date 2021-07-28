package com.iamport.sdk.presentation.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.google.gson.GsonBuilder
import com.iamport.sdk.data.chai.CHAI
import com.iamport.sdk.data.sdk.IamPortApprove
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.data.sdk.ProvidePgPkg
import com.iamport.sdk.domain.core.IamportReceiver
import com.iamport.sdk.domain.di.IamportKoinComponent
import com.iamport.sdk.domain.service.ChaiService
import com.iamport.sdk.domain.utils.*
import com.iamport.sdk.domain.utils.Util.observeAlways
import com.iamport.sdk.presentation.contract.BankPayContract
import com.iamport.sdk.presentation.contract.ChaiContract
import com.iamport.sdk.presentation.viewmodel.MainViewModel
import com.orhanobut.logger.Logger.*
import org.koin.androidx.viewmodel.compat.ViewModelCompat.viewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import java.lang.ref.WeakReference
import java.util.*


/**
 * 사실상 여기가 activity 같은 역할
 */
@KoinApiExtension
internal class IamportSdk(
    val activity: WeakReference<ComponentActivity>? = null,
    val fragment: WeakReference<Fragment>? = null,
    val webViewActivityLauncher: ActivityResultLauncher<Payment>?,
) : IamportKoinComponent {

    private val hostHelper: HostHelper = HostHelper(activity, fragment)

        private val mainViewModel: MainViewModel by viewModel(hostHelper.getViewModelStoreOwner()!!, MainViewModel::class.java) // 요청할 뷰모델
//    private val mainViewModel by lazy { ViewModelProvider(hostHelper.viewModelStoreOwner).get(MainViewModel::class.java) }

    // 전달받은 결제 결과 콜백
    private var paymentResultCallBack: ((IamPortResponse?) -> Unit)? = null // 콜백함수

    // 웹뷰 모드의 웹뷰
    private var modeWebViewRef: WeakReference<WebView>? = null

    // 웹뷰모드 & 모바일 웹 모드를 동작할 클래스
    private var iamPortWebViewMode: IamPortWebViewMode? = null
    private var iamPortMobileWebMode: IamPortMobileWebMode? = null
    // ---------------------------------------------

    // 뱅크페이 앱 런처s
    private var bankPayLauncher: ActivityResultLauncher<String>? = null // 뱅크페이 앱 런처(for webview & mobile web mode)
    private val bankPayContract by lazy { BankPayContract() }

    // 차이 앱 런처
    private var launcherChai: ActivityResultLauncher<Pair<String, String>>? = null // 차이앱 런처
    private val chaiContract by lazy { ChaiContract() }

    // 차이결제 최종 확인 전 콜백
    private var chaiApproveCallBack: ((IamPortApprove) -> Unit)? = null // 콜백함수

    // 차이앱 폴링여부
    private val isPolling = MutableLiveData<Event<Boolean>>()
    private val preventOverlapRun by lazy { PreventOverlapRun() }// 딜레이 호출

    // 포그라운드 서비스 관련 BroadcastReceiver
    private val iamportReceiver: IamportReceiver by inject()

    // =============================================
    // 스크린 on/off 감지 BroadcastReceiver
    private val screenBrReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> ScreenChecker.isScreenOn = true
                Intent.ACTION_SCREEN_OFF -> ScreenChecker.isScreenOn = false
            }
            d(intent?.action.toString())
        }
    }

    private val screenBrFilter = IntentFilter(Intent.ACTION_SCREEN_OFF).apply {
        addAction(Intent.ACTION_SCREEN_ON)
    }

    // =============================================

    init {

//        hostHelper.getViewModelStoreOwner()?.let {
//            mainViewModel = ViewModelProvider(it).get(MainViewModel::class.java)
//        } ?: run {
//            e("mainViewModel 를 생성할 수 없음")
//            return@run
//        }

        when (hostHelper.mode) {
            MODE.ACTIVITY -> {
                hostHelper.getActivityRef()?.run {
                    launcherChai = registerForActivityResult(chaiContract) { resultCallback() }
                    bankPayLauncher = registerForActivityResult(bankPayContract) {
                        if (it != null) {
                            resultBankPayAppCallback(it)
                        }
                    }
                }
            }
            MODE.FRAGMENT -> {
                hostHelper.getFragmentRef()?.run {
                    launcherChai = registerForActivityResult(chaiContract) { resultCallback() }
                    bankPayLauncher = registerForActivityResult(bankPayContract) {
                        if (it != null) {
                            resultBankPayAppCallback(it)
                        }
                    }
                }
            }
            MODE.NONE -> {
                e("HostHelper 모드가 NONE 입니다. activity [$activity], fragment [$fragment]")
            }
        }

        ScreenChecker.init(mainViewModel.app)

        initClearData()
    }

    // =============================================
    // webview 사용 모드
    fun enableWebViewMode(webviewRef: WeakReference<WebView>) {
        this.modeWebViewRef = webviewRef
    }

    fun disableWebViewMode() {
        this.modeWebViewRef = null
    }

    fun isWebViewMode(): Boolean {
        return this.modeWebViewRef != null
    }

    // mobile web standalone 사용 모드
    fun pluginMobileWebSupporter(webviewRef: WeakReference<WebView>) {
        hostHelper.getActivityRef()?.let {
            webviewRef.get()?.let { webview ->
                iamPortMobileWebMode = IamPortMobileWebMode(bankPayLauncher)
                iamPortMobileWebMode?.initStart(it, webview) // webview only 모드
            }
        }
    }
    // =============================================

    private val lifecycleObserver = object : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            d("onStart")
            mainViewModel.checkChaiStatus()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            d("onStop")
            mainViewModel.pollingChaiStatus() // 백그라운드 진입시 차이 폴링 시작, (webview 이용시에는 폴링하지 않음)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            d("onDestroy")
            initClearData()// FIXME : 이부분 체크
        }
    }
    // =============================================

    private fun initClearData() {
        clearData() // FIXME : 이부분 체크

        // 이때 hostHelper, mainViewModel 있음

        // 앱 lifecycleObserver 초기화
        hostHelper.getLifecycle()?.removeObserver(lifecycleObserver)

        // 포그라운드 서비스 관련 BroadcastReceiver,
        // 스크린 ON/OFF 브로드캐스트 리시버 초기화
        runCatching {
            mainViewModel.app.unregisterReceiver(iamportReceiver)
            mainViewModel.app.applicationContext?.unregisterReceiver(screenBrReceiver)
        }
    }

    // FIXME : 이부분 체크
    private fun closeDeleteWebViewMode() {
        iamPortWebViewMode?.close()
        iamPortMobileWebMode?.close()

        iamPortWebViewMode = null
        iamPortMobileWebMode = null
    }

    // FIXME : 이부분 체크
    fun close() {
        d("do Close! $iamPortWebViewMode")
        closeDeleteWebViewMode()
        disableWebViewMode()
        initClearData()
    }

    // FIXME : 이부분 체크
    fun failFinish() {
        mainViewModel.failSdkFinish()
    }

    /**
     * 본인인증 요청시 실행
     */
    fun initStart(payment: Payment, paymentResultCallBack: ((IamPortResponse?) -> Unit)?) {
        i("HELLO I'MPORT SDK! for cert")
        initClearData()// FIXME : 이부분 체크

        IntentFilter().let {
            it.addAction(CONST.BROADCAST_FOREGROUND_SERVICE)
            it.addAction(CONST.BROADCAST_FOREGROUND_SERVICE_STOP)
            mainViewModel.app.applicationContext?.registerReceiver(screenBrReceiver, screenBrFilter)
            mainViewModel.app.registerReceiver(iamportReceiver, it)
        }

        this.paymentResultCallBack = paymentResultCallBack

        hostHelper.getLifecycle()?.addObserver(lifecycleObserver)

        observeCertification(payment) // 관찰할 LiveData
    }

    /**
     * 결제 요청시 실행
     */
    fun initStart(payment: Payment, approveCallback: ((IamPortApprove) -> Unit)?, paymentResultCallBack: ((IamPortResponse?) -> Unit)?) {
        i("HELLO I'MPORT SDK! for payment")
        initClearData() // FIXME : 이부분 체크

        IntentFilter().let {
            it.addAction(CONST.BROADCAST_FOREGROUND_SERVICE)
            it.addAction(CONST.BROADCAST_FOREGROUND_SERVICE_STOP)
            mainViewModel.app.applicationContext?.registerReceiver(screenBrReceiver, screenBrFilter)
            mainViewModel.app.registerReceiver(iamportReceiver, it)
        }

        this.chaiApproveCallBack = approveCallback
        this.paymentResultCallBack = paymentResultCallBack

        hostHelper.getLifecycle()?.addObserver(lifecycleObserver)

        observeViewModel(payment) // 관찰할 LiveData
    }

    /**
     * 관찰할 LiveData 옵저빙
     */
    private fun observeViewModel(payment: Payment) {

        mainViewModel.payment = payment

        d(GsonBuilder().setPrettyPrinting().create().toJson(payment))
        hostHelper.getLifecycleOwner()?.let { owner: LifecycleOwner ->

            // 결제결과 옵저빙
            mainViewModel.impResponse().observe(owner, EventObserver(this::sdkFinish))

            // 웹뷰앱 열기
            mainViewModel.webViewActivityPayment().observe(owner, EventObserver(this::requestWebViewActivityPayment))

            // 차이앱 열기
            mainViewModel.chaiUri().observe(owner, EventObserver(this::openChaiApp))

            // 차이폴링여부
            mainViewModel.isPolling().observeAlways(owner, EventObserver {
                updatePolling(it)
                controlForegroundService(it)
            })

            // 차이 결제 상태 approve 처리
            mainViewModel.chaiApprove().observeAlways(owner, EventObserver(this::askApproveFromChai))

        }

        // 결제 시작
        preventOverlapRun.launch { requestPayment(payment) }
    }


    private fun observeCertification(payment: Payment) {
        d(GsonBuilder().setPrettyPrinting().create().toJson(payment))
        hostHelper.getLifecycleOwner()?.let { owner: LifecycleOwner ->

            // 결제결과 옵저빙
            mainViewModel.impResponse().observe(owner, EventObserver(this::sdkFinish))

            // 웹뷰앱 열기
            mainViewModel.webViewActivityPayment().observe(owner, EventObserver(this::requestWebViewActivityPayment))
        }

        // 본인인증 요청
        preventOverlapRun.launch { requestCertification(payment) }
    }

    fun mobileWebModeShouldOverrideUrlLoading(): LiveData<Event<Uri>>? {
        return iamPortMobileWebMode?.detectShouldOverrideUrlLoading()
    }

    fun isPolling(): LiveData<Event<Boolean>> {
        return isPolling
    }

    private fun updatePolling(it: Boolean) {
        isPolling.value = Event(it)
    }

    private fun controlForegroundService(it: Boolean) {
        if (!ChaiService.enableForegroundService) {
            d("차이 폴링 포그라운드 서비스 실행하지 않음")
            return
        }

        mainViewModel.app.run {
            Intent(this, ChaiService::class.java).also { intent: Intent ->
                if (it) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                } else {
                    stopService(intent)
                }
            }
        }
    }

    private fun askApproveFromChai(approve: IamPortApprove) {
        chaiApproveCallBack?.run {
            invoke(approve)
        } ?: run {
            requestApprovePayments(approve)
        }
    }

    // 차이 최종 결제 요청
    fun requestApprovePayments(approve: IamPortApprove) {
        mainViewModel.requestApprovePayments(approve)
    }

    /**
     * 차이 앱 종료 콜백 감지
     */
    private fun resultCallback() {
        d("Result Callback ChaiLauncher")
        mainViewModel.checkChaiStatusForResultCallback()
    }

    /**
     * 나이스 뱅크페이 앱 종료 콜백 감지 for 웹뷰모드, 모바일웹모드
     */
    private fun resultBankPayAppCallback(resPair: Pair<String, String>) {
        d("Result Callback BankPayLauncher")
        if (modeWebViewRef?.get() != null) {
            iamPortWebViewMode?.processBankPayPayment(resPair)
            return
        }
        iamPortMobileWebMode?.processBankPayPayment(resPair)
    }


    /**
     * 결제 요청 실행
     */
    private fun requestPayment(payment: Payment) {
        Payment.validator(payment).run {
            if (!first) {
                sdkFinish(second?.let { IamPortResponse.makeFail(payment, msg = it) })
                return
            }
        }

        // 네트워크 연결 상태 체크
        if (!Util.isInternetAvailable(mainViewModel.app)) {
            sdkFinish(IamPortResponse.makeFail(payment, msg = "네트워크 연결 안됨"))
            return
        }

        // webview mode 라면 네이티브 연동 사용하지 않음
        // 동작의 문제는 없으나 UI 에서 표현하기 애매함
        if (modeWebViewRef?.get() != null) {
            mainViewModel.judgePayment(payment, ignoreNative = true)
            return
        }

        mainViewModel.judgePayment(payment) // 뷰모델에 데이터 판단 요청(native or webview pg)
    }

    /**
     * 결제 요청 실행
     */
    private fun requestCertification(payment: Payment) {
        // 네트워크 연결 상태 체크
        if (!Util.isInternetAvailable(mainViewModel.app)) {
            sdkFinish(IamPortResponse.makeFail(payment, msg = "네트워크 연결 안됨"))
            return
        }

        mainViewModel.judgePayment(payment) // 뷰모델에 데이터 판단 요청(native or webview pg)
    }


    /**
     * 웹뷰 결제 요청 실행
     */
    private fun requestWebViewActivityPayment(payment: Payment) {
        d("request WebViewActivity Payment $payment")
        clearData()
        modeWebViewRef?.get()?.let { webView ->
            hostHelper.getActivityRef()?.let { activity ->
                iamPortWebViewMode = IamPortWebViewMode(bankPayLauncher)
                iamPortWebViewMode?.initStart(activity, webView, payment, paymentResultCallBack) // webview only 모드
            } ?: run {
                w("Cannot found activity, So running activity mode")
                webViewActivityLauncher?.launch(payment) // new activity 모드
            }
        } ?: run {
            webViewActivityLauncher?.launch(payment) // new activity 모드
        }
    }


    /**
     * 뷰모델 데이터 클리어
     */
    private fun clearData() {
        d("clearData!")
        updatePolling(false) // 차이 폴링 외부 인터페이스 초기화
        controlForegroundService(false) // 차이 포그라운드 서비스 초기화
        mainViewModel.clearData() // 메인 뷰모델 클리어
    }


    /**
     * 모든 결과 처리 및 SDK 종료
     */
    private fun sdkFinish(iamPortResponse: IamPortResponse?) {
        i("SDK Finish")
        d(iamPortResponse.toString())
        closeDeleteWebViewMode() // FIXME: 필요할까?

        initClearData()
        paymentResultCallBack?.invoke(iamPortResponse)
    }


    /**
     * 차이앱 외부앱 열기
     */
    private fun openChaiApp(it: String) {
        i("openChaiApp")
        d(it)
        runCatching {
            launcherChai?.launch(it to "openchai")
            mainViewModel.playChai = true
            CHAI.pkg = getIntentPackage(Intent.parseUri(it, Intent.URI_INTENT_SCHEME))?.also {
                mainViewModel.chaiClearVersion = checkChaiVersionCode(it)
            }
        }.onFailure { thr: Throwable ->
            i("${thr.message}")
            movePlayStore(Intent.parseUri(it, Intent.URI_INTENT_SCHEME))
            clearData()
        }
    }

    private fun getIntentPackage(intent: Intent): String? {
        return intent.`package` ?: run {
            // intent 에 패키지 없으면 ProvidePgPkg에서 intnet.schme 으로 앱 패키지 검색
            i("Not found in intent package")
            when (val providePgPkg = intent.scheme?.let { ProvidePgPkg.from(it) }) {
                null -> {
                    i("Not found in intent schme")
                    d("Not found in intent schme :: ${intent.scheme}")
                    return@run null
                }
                else -> providePgPkg.pkg
            }
        }
    }


    /**
     * 앱 패키지 검색하여 플레이 스토어로 이동
     */
    private fun movePlayStore(intent: Intent) {
        getIntentPackage(intent)?.let {
            d("movePlayStore :: $it")
            Intent(Intent.ACTION_VIEW, Uri.parse(Util.getMarketId(it))).run {
                flags = Intent.FLAG_ACTIVITY_NO_USER_ACTION
                if (hostHelper.mode == MODE.ACTIVITY) {
                    hostHelper.getActivityRef()?.startActivity(this)
                } else {
                    hostHelper.getFragmentRef()?.startActivity(this)
                }
            }
        }
    }

    private fun checkChaiVersionCode(chaiPackageName: String): Boolean {
        var versionCode = CHAI.SINGLE_ACTIVITY_VERSION

        runCatching {
            versionCode = Util.versionCode(mainViewModel.app, chaiPackageName).toLong()
            d("chai app version : $versionCode")
        }.onFailure {
            i("Fail to get chai app version [${it.message}]")
        }

        return versionCode > CHAI.SINGLE_ACTIVITY_VERSION
    }
}
