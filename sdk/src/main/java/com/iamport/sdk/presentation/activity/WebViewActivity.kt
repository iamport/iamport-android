package com.iamport.sdk.presentation.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.iamport.sdk.R
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.data.sdk.ProvidePgPkg
import com.iamport.sdk.databinding.WebviewActivityBinding
import com.iamport.sdk.domain.IamportWebChromeClient
import com.iamport.sdk.domain.JsNativeInterface
import com.iamport.sdk.domain.di.IamportKoinComponent
import com.iamport.sdk.domain.utils.*
import com.iamport.sdk.presentation.contract.BankPayContract
import com.iamport.sdk.presentation.viewmodel.WebViewModel
import com.orhanobut.logger.Logger.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension


@KoinApiExtension
class WebViewActivity : BaseActivity<WebviewActivityBinding, WebViewModel>(), IamportKoinComponent {

    override val layoutResourceId: Int = R.layout.webview_activity
    override val viewModel: WebViewModel by viewModel()

    private lateinit var loading: ProgressBar
    private var payment: Payment? = null

    /**
     * 뱅크페이 앱 열기 위한 런처
     */
    private var launcherBankPay =
        registerForActivityResult(BankPayContract()) { res: Pair<String, String>? ->
            res?.let {
                loadingVisible(true)
                viewModel.processBankPayPayment(res)
            } ?: e("NICE TRANS result is NULL")
        }

    /**
     * BaseActivity 에서 onCreate 시 호출
     */
    override fun initStart() {
        i("HELLO I'MPORT WebView SDK!")

        initLoading()

        // intent 로 부터 전달받은 Payment 객체
        val bundle = intent.getBundleExtra(CONST.CONTRACT_INPUT)
        payment = bundle?.getParcelable(CONST.BUNDLE_PAYMENT)

        observeViewModel(payment) // 관찰할 LiveData
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
        loading = viewDataBinding.loading as ProgressBar
        loadingVisible(true)
    }


    /**
     * 액티비티 알파값 조정
     */
    private fun updateAlpha(isWebViewPG: Boolean) {
        val alpha = if (isWebViewPG) 1.0F else 0.0F
        viewDataBinding.webviewActivity.alpha = alpha
    }

    /**
     * 관찰할 LiveData 옵저빙
     */
    private fun observeViewModel(payment: Payment?) {
        d(GsonBuilder().setPrettyPrinting().create().toJson(payment))
        payment?.let { pay: Payment ->

            viewModel.payment().observe(this, EventObserver(this::requestPayment))
            viewModel.loading().observe(this, EventObserver(this::loadingVisible))

            viewModel.openWebView().observe(this, EventObserver(this::openWebView))
            viewModel.niceTransRequestParam().observe(this, EventObserver(this::openNiceTransApp))
            viewModel.thirdPartyUri().observe(this, EventObserver(this::openThirdPartyApp))

            viewModel.impResponse().observe(this, EventObserver(this::sdkFinish))

            viewModel.startPayment(pay)
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
    private fun requestPayment(it: Payment) {
        loadingVisible(true)
        if (!Util.isInternetAvailable(this)) {
            sdkFinish(IamPortResponse.makeFail(it, msg = "네트워크 연결 안됨"))
            return
        }
        viewModel.requestPayment(it)
    }

    override fun onBackPressed() {
        viewDataBinding.webview.run {
            if (canGoBack()) {
                goBack()
            } else {
                super.onBackPressed()
            }
        }
    }

    /**
     * 모든 결과 처리 및 SDK 종료
     */
    override fun sdkFinish(iamPortResponse: IamPortResponse?) {
        i("call sdkFinish")
        d("sdkFinish => ${iamPortResponse.toString()}")
        loadingVisible(false)
        setResult(Activity.RESULT_OK,
            Intent().apply { putExtra(CONST.CONTRACT_OUTPUT, iamPortResponse) })
        this.finish()
    }

    /**
     * 뱅크페이 외부앱 열기 for nice PG + 실시간계좌이체(trans)
     */
    private fun openNiceTransApp(it: String) {
        runCatching {
            launcherBankPay.launch(it) // 뱅크페이 앱 실행
        }.onFailure {
            // 뱅크페이 앱 패키지는 하드코딩
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Util.getMarketId(ProvidePgPkg.BANKPAY.pkg))))
        }
        loadingVisible(false)
    }


    /**
     * 외부앱 열기
     */
    private fun openThirdPartyApp(it: Uri) {
        d("openThirdPartyApp $it")
        Intent.parseUri(it.toString(), Intent.URI_INTENT_SCHEME)?.let { intent: Intent ->
            runCatching {
                startActivity(intent)
            }.onFailure {
                movePlayStore(intent)
            }
        }
        loadingVisible(false)
    }


    /**
     * 앱 패키지 검색하여 플레이 스토어로 이동
     */
    private fun movePlayStore(intent: Intent) {
        val pkg = intent.`package` ?: run {
            // intent 에 패키지 없으면 ProvidePgPkg에서 intnet.schme 으로 앱 패키지 검색
            i("Not found intent package")
            when (val providePgPkg = intent.scheme?.let { ProvidePgPkg.from(it) }) {
                null -> {
                    e("Not found intent schme :: ${intent.scheme}")
                    return@run null
                }
                else -> providePgPkg.pkg
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
    private fun openWebView(payment: Payment) {
        d("오픈! 웹뷰")

        val evaluateJS = fun(jsMethod: String) {
            val js = "javascript:$jsMethod"
            d("evaluateJS => $js")
            launch {
                viewDataBinding.webview.run {
                    this.loadUrl(js)
                }
            }
        }

        setTheme(R.style.Theme_AppCompat_Transparent_NoActionBar)
        updateAlpha(true)
        loadingVisible(true)

        viewDataBinding.webview.run {
            fitsSystemWindows = true
            settingsWebView(this)
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            clearCache(true)
            addJavascriptInterface(
                JsNativeInterface(payment, get(), get(), evaluateJS),
                CONST.PAYMENT_WEBVIEW_JS_INTERFACE_NAME
            )
            webViewClient = viewModel.getWebViewClient(payment)
            visibility = View.VISIBLE

            loadUrl(CONST.PAYMENT_FILE_URL) // load WebView
            webChromeClient = IamportWebChromeClient()
        }
    }


}