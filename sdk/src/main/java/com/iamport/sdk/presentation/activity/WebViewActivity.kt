package com.iamport.sdk.presentation.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.WebView
import android.widget.ProgressBar
import com.google.gson.GsonBuilder
import com.iamport.sdk.R
import com.iamport.sdk.data.sdk.IamportResponse
import com.iamport.sdk.data.sdk.IamportRequest
import com.iamport.sdk.data.sdk.ProvidePgPkg
import com.iamport.sdk.domain.IamportWebChromeClient
import com.iamport.sdk.domain.JsNativeInterface
import com.iamport.sdk.domain.di.IamportKoinComponent
import com.iamport.sdk.domain.utils.*
import com.iamport.sdk.presentation.viewmodel.WebViewModel
import com.orhanobut.logger.Logger.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named


class WebViewActivity : BaseActivity<WebViewModel>(), IamportKoinComponent {

    override val layoutResourceId: Int = R.layout.webview_activity
    override val viewModel: WebViewModel by viewModel()

    private lateinit var loading: ProgressBar
    private lateinit var webview: WebView
    private var request: IamportRequest? = null

    override fun onDestroy() {
        runCatching {
            removeObservers()
        }.onFailure {
            d("ignore fail close webview $it")
        }
        super.onDestroy()
    }

    override fun initStart() {
        i("HELLO I'MPORT WebView SDK!")

        loading = findViewById(R.id.loading)
        webview = findViewById(R.id.webview)

        initLoading()

        // intent 로 부터 전달받은 Payment 객체
        val bundle = intent.getBundleExtra(Constant.CONTRACT_INPUT)
        request = bundle?.getParcelable(Constant.BUNDLE_PAYMENT)

        observeViewModel(request) // 관찰할 LiveData
    }

    override fun onNewIntent(intent: Intent?) {
        d("onNewIntent")
        super.onNewIntent(intent)
        this.intent = intent
//        removeObserveViewModel(payment)
        initStart()
    }

    /**
     * 로딩 UI 초기화
     */
    private fun initLoading() {
        if (request != null) {
            loadingVisible(true)
        }
    }


//    /**
//     * 액티비티 알파값 조정
//     */
//    private fun updateAlpha(isWebViewPG: Boolean) {
//        val alpha = if (isWebViewPG) 1.0F else 0.0F
//        viewDataBinding.webviewActivity.alpha = alpha
//    }

    /**
     * 관찰할 LiveData 옵저빙
     */
    override fun observeViewModel(request: IamportRequest?) {
        d(GsonBuilder().setPrettyPrinting().create().toJson(request))
        request?.let { pay: IamportRequest ->

            viewModel.loading().observe(this, EventObserver(this::loadingVisible))

            viewModel.openWebView().observe(this, EventObserver(this::openWebView))

//            viewModel.niceTransRequestParam().observe(this, EventObserver(this::openNiceTransApp))
            viewModel.thirdPartyUri().observe(this, EventObserver(this::openThirdPartyApp))

            viewModel.impResponse().observe(this, EventObserver(this::sdkFinish))

            requestPayment(pay)
        }
    }

    /**
     * 로딩 프로그래스 visible 여부
     */
    private fun loadingVisible(visible: Boolean) {
        loading.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * 결제 요청 실행
     */
    override fun requestPayment(it: IamportRequest) {
        loadingVisible(true)
        if (!Util.isInternetAvailable(this)) {
            sdkFinish(IamportResponse.makeFail(it, msg = "네트워크 연결 안됨"))
            return
        }
        viewModel.requestPayment(it)
    }

    private fun removeObservers() {
        runCatching {
            d("WebViewActivity removeObservers")
            viewModel.loading().removeObservers(this)
            viewModel.openWebView().removeObservers(this)
//            viewModel.niceTransRequestParam().removeObservers(this)
            viewModel.thirdPartyUri().removeObservers(this)
            viewModel.impResponse().removeObservers(this)
        }.onFailure {
            e("Fail WebViewActivity removeObservers$it")
        }
    }

    private fun close() {
        runCatching {
            d("WebViewActivity close")
            removeObservers()

            webview.run {
                removeJavascriptInterface(Constant.PAYMENT_WEBVIEW_JS_INTERFACE_NAME)
                clearHistory()
                loadUrl("about:blank")
                removeAllViews()
                destroy()
            }
        }.onFailure {
            e("Fail WebViewActivity close$it")
        }
    }

    /**
     * 모든 결과 처리 및 SDK 종료
     */
    override fun sdkFinish(iamPortResponse: IamportResponse?) {
        i("call sdkFinish")
        d("sdkFinish => ${iamPortResponse.toString()}")

        close()
        loadingVisible(false)
        setResult(Activity.RESULT_OK, Intent().apply { putExtra(Constant.CONTRACT_OUTPUT, iamPortResponse) })
//        Iamport.callback(iamPortResponse)

        this.finish()
    }

    /**
     * 뱅크페이 외부앱 열기 for nice PG + 실시간계좌이체(trans)
     */
//    override fun openNiceTransApp(it: String) {
//        runCatching {
//            launcherBankPay.launch(it) // 뱅크페이 앱 실행
//        }.onFailure {
//             뱅크페이 앱 패키지는 하드코딩
//            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Util.getMarketId(ProvidePgPkg.BANKPAY.pkg))))
//        }
//        loadingVisible(false)
//    }

    /**
     * 외부앱 열기
     */
    override fun openThirdPartyApp(it: Uri) {
        d("openThirdPartyApp $it")
        Intent.parseUri(it.toString(), Intent.URI_INTENT_SCHEME)?.let { intent: Intent ->
            runCatching {
                startActivity(intent)
            }.recoverCatching {
                movePlayStore(intent)
            }.onFailure {
                i("설치 버튼을 이용하여 앱을 설치하세요.")
            }
        }
        loadingVisible(false)
    }


    /**
     * 앱 패키지 검색하여 플레이 스토어로 이동
     */
    override fun movePlayStore(intent: Intent) {
        val pkg = intent.`package` ?: run {
            // intent 에 패키지 없으면 ProvidePgPkg에서 intnet.schme 으로 앱 패키지 검색
            i("Not found intent package")
            when (val providePgPkg = intent.scheme?.let { ProvidePgPkg.from(it) }) {
                null -> {
                    e("Not found intent schme :: ${intent.scheme}")
                    return@run null
                }
                else -> {
                    d("Found pkg : ${providePgPkg.pkg}")
                    providePgPkg.pkg
                }
            }
        }

        if (!pkg.isNullOrBlank()) {
            d("movePlayStore :: $pkg")
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Util.getMarketId(pkg))))
        }
    }


    /**
     * 웹뷰 오픈
     */
    override fun openWebView(request: IamportRequest) {
        d("오픈! 웹뷰")

        val evaluateJS = fun(jsMethod: String) {
            val js = "javascript:$jsMethod"
            d("evaluateJS => $js")
            launch {
                webview.run {
                    this.loadUrl(js)
                }
            }
        }

        setTheme(R.style.Theme_AppCompat_Transparent_NoActionBar)
//        updateAlpha(true)
        loadingVisible(true)

        webview.run {
            fitsSystemWindows = true
            settingsWebView(this)
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            clearCache(true)
            addJavascriptInterface(
                JsNativeInterface(request, get(named("${Constant.KOIN_KEY}Gson")), evaluateJS),
                Constant.PAYMENT_WEBVIEW_JS_INTERFACE_NAME
            )
            webViewClient = viewModel.getWebViewClient()
            visibility = View.VISIBLE

            loadUrl(Constant.PAYMENT_FILE_URL) // load WebView
            webChromeClient = IamportWebChromeClient()
        }
    }


}