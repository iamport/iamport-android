package com.iamport.sdk.presentation.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.google.gson.GsonBuilder
import com.iamport.sdk.data.chai.CHAI
import com.iamport.sdk.data.sdk.IamportApprove
import com.iamport.sdk.data.sdk.IamportResponse
import com.iamport.sdk.data.sdk.IamportRequest
import com.iamport.sdk.data.sdk.ProvidePgPkg
import com.iamport.sdk.domain.core.IamportReceiver
import com.iamport.sdk.domain.di.IamportKoinComponent
import com.iamport.sdk.domain.utils.*
import com.iamport.sdk.domain.utils.Util.observeAlways
import com.iamport.sdk.presentation.contract.ChaiContract
import com.iamport.sdk.presentation.viewmodel.MainViewModel
import com.iamport.sdk.presentation.viewmodel.MainViewModelFactory
import com.orhanobut.logger.Logger.*
import org.koin.core.component.get
import org.koin.core.component.inject
import java.lang.ref.WeakReference


/**
 * 사실상 여기가 activity 같은 역할
 */
internal class IamportSdk(
    val activity: WeakReference<ComponentActivity>? = null,
    val fragment: WeakReference<Fragment>? = null,
    val webViewActivityLauncher: ActivityResultLauncher<IamportRequest>?,
) : IamportKoinComponent {

    private val hostHelper: HostHelper = HostHelper(activity, fragment)

    //    private val mainViewModel: MainViewModel by viewModel(hostHelper.getViewModelStoreOwner(), MainViewModel::class.java) // 요청할 뷰모델
    private val mainViewModel by lazy {
        hostHelper.getViewModelStoreOwner()?.let {
            ViewModelProvider(it, MainViewModelFactory(get(), get(), get())).get(MainViewModel::class.java)
        }
    }

    // 전달받은 결제 결과 콜백
    private var paymentResultCallBack: ((IamportResponse?) -> Unit)? = null // 콜백함수

    // 웹뷰 모드의 웹뷰
    private var modeWebViewRef: WeakReference<WebView>? = null

    // 웹뷰모드 & 모바일 웹 모드를 동작할 클래스
    private var iamportWebViewMode: IamportWebViewMode? = null
    private var iamportMobileWebMode: IamportMobileWebMode? = null

    // 차이 앱 런처
    private var launcherChai: ActivityResultLauncher<Pair<String, String>>? = null // 차이앱 런처
    private val chaiContract by lazy { ChaiContract() }

    // 차이결제 최종 확인 전 콜백
    private var chaiApproveCallBack: ((IamportApprove) -> Unit)? = null // 콜백함수

    // 차이앱 폴링여부
    private val isPolling = MutableLiveData<Event<Boolean>>()
    private val preventOverlapRun by lazy { PreventOverlapRun() }// 딜레이 호출

    // 포그라운드 서비스 관련 BroadcastReceiver
    private val iamportReceiver: IamportReceiver by inject()

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

    init {

        when (hostHelper.mode) {
            MODE.ACTIVITY -> {
                hostHelper.getActivityRef()?.run {
                    launcherChai = registerForActivityResult(chaiContract) { resultCallback() }
                }
            }
            MODE.FRAGMENT -> {
                hostHelper.getFragmentRef()?.run {
                    launcherChai = registerForActivityResult(chaiContract) { resultCallback() }
                }
            }
            MODE.NONE -> {
                e("HostHelper 모드가 NONE 입니다. activity [$activity], fragment [$fragment]")
            }
        }

        mainViewModel?.let {
            ScreenChecker.init(it.app)
        }

        updatePolling(false) // 차이 폴링 외부 인터페이스 초기화
        mainViewModel?.controlForegroundService(false) // 차이 포그라운드 서비스 초기화
        mainViewModel?.unregisterIamportReceiver(iamportReceiver, screenBrReceiver)
    }

    // =============================================
    // webview 사용 모드
    fun enableWebViewMode(webviewRef: WeakReference<WebView>) {
        this.modeWebViewRef = webviewRef
    }

    fun disableWebViewMode() {
        modeWebViewRef?.clear()
        modeWebViewRef = null
    }

    fun isWebViewMode(): Boolean {
        return modeWebViewRef?.get() != null
    }

    // mobile web standalone 사용 모드
    fun pluginMobileWebSupporter(webviewRef: WeakReference<WebView>) {
        disableWebViewMode() // 모바일 웹 모드 시작할 때
        closeDeleteWebViewMode() // 모바일 웹 모드 시작할 때
        initClearData() // 모바일 웹 모드 시작할 때

        hostHelper.getActivityRef()?.let {
            webviewRef.get()?.let { webview ->
                iamportMobileWebMode = IamportMobileWebMode()
                iamportMobileWebMode?.initStart(it, webview) // webview only 모드
            }
        }
    }

    /**
     * 모든 결과 처리 및 SDK 종료
     */
    private fun sdkFinish(iamPortResponse: IamportResponse?) {
        i("SDK Finish")
        d(iamPortResponse.toString())

        closeDeleteWebViewMode() // sdk 끝나는거니까 필요함
        initClearData() // sdk 끝나고 나갈때

        paymentResultCallBack?.invoke(iamPortResponse)
    }

    /**
     * 뷰모델 데이터 클리어
     */
    private fun clearMainViewModel() {
        d("clearMainViewModel!")

        updatePolling(false) // 차이 폴링 외부 인터페이스 초기화
        mainViewModel?.controlForegroundService(false) // 차이 포그라운드 서비스 초기화
        mainViewModel?.clearData() // 메인 뷰모델 클리어
    }


    // clearData + lifecycleObserver, iamportReceiver, screenBrReceiver 초기화
    private fun initClearData() {
        clearMainViewModel()
        mainViewModel?.unregisterIamportReceiver(iamportReceiver, screenBrReceiver)
//        hostHelper.getLifecycle()?.removeObserver(lifecycleObserver)
    }

    private fun closeDeleteWebViewMode() {
        iamportWebViewMode?.close()
        iamportMobileWebMode?.close()

        iamportWebViewMode = null
        iamportMobileWebMode = null
    }

    // 외부 종료
    fun close() {
        d("do Close!")

        disableWebViewMode()
        closeDeleteWebViewMode() // 외부에서 종료하니까 필요함
        initClearData() // 밖에서 강제로 끌때
    }

    fun initClose() {
        d("do initClose!")

        disableWebViewMode() // 웹뷰모드 끄기
        closeDeleteWebViewMode() // 초기화 하니까 필요함

        updatePolling(false) // 차이 폴링 외부 인터페이스 초기화
        mainViewModel?.controlForegroundService(false) // 차이 포그라운드 서비스 초기화
        mainViewModel?.unregisterIamportReceiver(iamportReceiver, screenBrReceiver)
    }

    // FIXME : 이부분 체크
    fun failFinish() {
        mainViewModel?.failSdkFinish()
    }

    /**
     * 본인인증 요청시 실행
     */
    fun initStart(request: IamportRequest, paymentResultCallBack: ((IamportResponse?) -> Unit)?) {
        i("HELLO I'MPORT SDK! for cert")

        closeDeleteWebViewMode() // 결제 시작할 때
//        initClearData() // 결제 시작할 때

        mainViewModel?.registerIamportReceiver(iamportReceiver, screenBrReceiver)
//        hostHelper.getLifecycle()?.addObserver(lifecycleObserver)

        this.paymentResultCallBack = paymentResultCallBack
        observeCertification(request) // 관찰할 LiveData
    }

    /**
     * 결제 요청시 실행
     */
    fun initStart(request: IamportRequest, approveCallback: ((IamportApprove) -> Unit)?, paymentResultCallBack: ((IamportResponse?) -> Unit)?) {
        i("HELLO I'MPORT SDK! for payment")

        closeDeleteWebViewMode() // 결제 시작할 때
//        initClearData() // 결제 시작할 때

        mainViewModel?.registerIamportReceiver(iamportReceiver, screenBrReceiver)
//        hostHelper.getLifecycle()?.addObserver(lifecycleObserver)

        this.chaiApproveCallBack = approveCallback
        this.paymentResultCallBack = paymentResultCallBack
        observeViewModel(request) // 관찰할 LiveData
    }

    /**
     * 관찰할 LiveData 옵저빙
     */
    private fun observeViewModel(request: IamportRequest) {

        mainViewModel?.request = request

        d(GsonBuilder().setPrettyPrinting().create().toJson(request))
        hostHelper.getLifecycleOwner()?.let { owner: LifecycleOwner ->

            mainViewModel?.let {

                // 결제결과 옵저빙
                it.impResponse().observe(owner, EventObserver(this::sdkFinish))

                // 웹뷰앱 열기
                it.webViewActivityPayment().observe(owner, EventObserver(this::requestWebViewActivityPayment))

                // 차이앱 열기
                it.chaiUri().observe(owner, EventObserver(this::openChaiApp))

                // 차이폴링여부
                it.isPolling().observeAlways(owner, EventObserver { isPolling ->
                    updatePolling(isPolling)
                    mainViewModel?.controlForegroundService(isPolling)
                })

                // 차이 결제 상태 approve 처리
                it.chaiApprove().observeAlways(owner, EventObserver(this::askApproveFromChai))

            }
        }

        // 결제 시작
        preventOverlapRun.launch { requestPayment(request) }
    }


    private fun observeCertification(request: IamportRequest) {
        d(GsonBuilder().setPrettyPrinting().create().toJson(request))
        hostHelper.getLifecycleOwner()?.let { owner: LifecycleOwner ->

            mainViewModel?.let {
                // 결제결과 옵저빙
                it.impResponse().observe(owner, EventObserver(this::sdkFinish))

                // 웹뷰앱 열기
                it.webViewActivityPayment().observe(owner, EventObserver(this::requestWebViewActivityPayment))
            }
        }

        // 본인인증 요청
        preventOverlapRun.launch { requestCertification(request) }
    }

    fun mobileWebModeShouldOverrideUrlLoading(): LiveData<Event<Uri>>? {
        return iamportMobileWebMode?.detectShouldOverrideUrlLoading()
    }

    fun isPolling(): LiveData<Event<Boolean>> {
        return isPolling
    }

    private fun updatePolling(it: Boolean) {
        isPolling.value = Event(it)
    }


    private fun askApproveFromChai(approve: IamportApprove) {
        when (mainViewModel?.approved) {
            MainViewModel.Status.Waiting, null -> {
                // 아무 동작하지 않음
            }
            MainViewModel.Status.None -> {
                mainViewModel?.approved = MainViewModel.Status.Waiting
                chaiApproveCallBack?.run {
                    invoke(approve)
                } ?: run {
                    requestApprovePayments(approve)
                }
            }
        }
    }

    // 차이 최종 결제 요청
    fun requestApprovePayments(approve: IamportApprove) {
        mainViewModel?.requestApprovePayments(approve)
    }

    /**
     * 차이 앱 종료 콜백 감지
     */
    private fun resultCallback() {
//        d("차이 앱이 종료됐지만 아무것도 안할게!!")
        d("Result Callback ChaiLauncher")
        mainViewModel?.forceChaiStatusCheck()
    }

    /**
     * 나이스 뱅크페이 앱 종료 콜백 감지 for 웹뷰모드, 모바일웹모드
     */
//    private fun resultBankPayAppCallback(resPair: Pair<String, String>) {
//        d("Result Callback BankPayLauncher")
//        if (modeWebViewRef?.get() != null) {
//            iamPortWebViewMode?.processBankPayPayment(resPair)
//            return
//        }
//        iamPortMobileWebMode?.processBankPayPayment(resPair)
//    }


    /**
     * 결제 요청 실행
     */
    private fun requestPayment(request: IamportRequest, ignoreNative: Boolean = false) {
        IamportRequest.validator(request).run {
            if (!first) {
                sdkFinish(second?.let { IamportResponse.makeFail(request, msg = it) })
                return
            }
        }

        // 네트워크 연결 상태 체크
        if (!Util.isInternetAvailable(mainViewModel?.app)) {
            sdkFinish(IamportResponse.makeFail(request, msg = "네트워크 연결 안됨"))
            return
        }

        // webview mode 라면 네이티브 연동 사용하지 않음
        // 동작의 문제는 없으나 UI 에서 표현하기 애매함
        if (modeWebViewRef?.get() != null) {
            mainViewModel?.judgePayment(request, ignoreNative = true)
            return
        }

        mainViewModel?.judgePayment(request, ignoreNative) // 뷰모델에 데이터 판단 요청(native or webview pg)
    }

    /**
     * 결제 요청 실행
     */
    private fun requestCertification(request: IamportRequest) {
        // 네트워크 연결 상태 체크
        if (!Util.isInternetAvailable(mainViewModel?.app)) {
            sdkFinish(IamportResponse.makeFail(request, msg = "네트워크 연결 안됨"))
            return
        }

        mainViewModel?.judgePayment(request) // 뷰모델에 데이터 판단 요청(native or webview pg)
    }


    /**
     * 웹뷰 결제 요청 실행
     */
    private fun requestWebViewActivityPayment(request: IamportRequest) {
        d("request WebViewActivity Payment $request")
        clearMainViewModel()
        modeWebViewRef?.get()?.let { webView ->
            hostHelper.getActivityRef()?.let { activity ->
                iamportWebViewMode = IamportWebViewMode()
                iamportWebViewMode?.initStart(activity, webView, request, paymentResultCallBack) // webview only 모드
            } ?: run {
                w("Cannot found activity, So running activity mode")
                webViewActivityLauncher?.launch(request) // new activity 모드
            }
        } ?: run {
            webViewActivityLauncher?.launch(request) // new activity 모드
        }
    }

    /**
     * 차이앱 외부앱 열기
     */
    private fun openChaiApp(it: String) {
        i("openChaiApp")
        d(it)

        var chaiClearVersion = false

        // 우선 chaiClearVersion 인지 체크
        runCatching {
            getIntentPackage(Intent.parseUri(it, Intent.URI_INTENT_SCHEME))?.also {
                chaiClearVersion = checkChaiVersionCode(it)
            }
        }.onFailure { thr: Throwable ->
            i("${thr.message}, chaiClearVersion 가져오는 도중 에러남. 네이티브 모드로 실행.")
        }

        // 차이 WebStrategy 로 동작
        if (!chaiClearVersion) {
            mainViewModel?.request?.let {
                clearMainViewModel() // WebStrategy 로 결제 재요청 전, 초기화
                requestPayment(it, true)
                return
            }
        }

        d("chaiClearVersion($chaiClearVersion) == false 면, 여기까지 안와야함")

        // 네이티브 차이 앱 실행
        runCatching {
            launcherChai?.launch(it to "openchai")
        }.onFailure { thr: Throwable ->
            i("${thr.message}")
            movePlayStore(Intent.parseUri(it, Intent.URI_INTENT_SCHEME))
            clearMainViewModel()
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
                when (hostHelper.mode) {
                    MODE.ACTIVITY -> hostHelper.getActivityRef()?.startActivity(this)
                    MODE.FRAGMENT -> hostHelper.getFragmentRef()?.startActivity(this)
                    MODE.NONE -> e("Fail move to movePlayStore")
                }
            }
        }
    }

    private fun checkChaiVersionCode(chaiPackageName: String): Boolean {
        var versionCode = CHAI.SINGLE_ACTIVITY_VERSION

        runCatching {
            versionCode = Util.versionCode(mainViewModel?.app, chaiPackageName).toLong()
            d("chai app version : $versionCode")
        }.onFailure {
            i("Fail to get chai app version [${it.message}]")
        }

        return versionCode > CHAI.SINGLE_ACTIVITY_VERSION
    }
}
