package com.iamport.sdk.presentation.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.google.gson.GsonBuilder
import com.iamport.sdk.BuildConfig
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.data.sdk.ProvidePgPkg
import com.iamport.sdk.domain.IamportWebChromeClient
import com.iamport.sdk.domain.JsNativeInterface
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.di.IamportKoinComponent
import com.iamport.sdk.domain.utils.*
import com.iamport.sdk.presentation.contract.BankPayContract
import com.iamport.sdk.presentation.viewmodel.WebViewModel
import com.orhanobut.logger.Logger.*
import kotlinx.coroutines.*
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.get
import org.koin.core.qualifier.named


@KoinApiExtension
class IamPortWebViewMode @JvmOverloads constructor(scope: BaseCoroutineScope = UICoroutineScope()) :
    IamportKoinComponent, BaseMain, BaseCoroutineScope by scope {

    private val viewModel: WebViewModel = WebViewModel(get(), get())

    private var payment: Payment? = null
    private var activity: ComponentActivity? = null
    private var webview: WebView? = null

    /**
     * 뱅크페이 앱 열기 위한 런처
     */
    private var launcherBankPay =
        activity?.registerForActivityResult(BankPayContract()) { res: Pair<String, String>? ->
            res?.let {
                viewModel.processBankPayPayment(res)
            } ?: e("NICE TRANS result is NULL")
        }

    /**
     * BaseActivity 에서 onCreate 시 호출
     */
    fun initStart(activity: ComponentActivity, webview: WebView, payment: Payment) {
        i("HELLO I'MPORT WebView SDK!")

        this.activity = activity
        this.payment = payment
        this.webview = webview

        onBackPressed()
        observeViewModel(payment) // 관찰할 LiveData
    }


    /**
     * 관찰할 LiveData 옵저빙
     */
    override fun observeViewModel(payment: Payment?) {
        d(GsonBuilder().setPrettyPrinting().create().toJson(payment))
        payment?.let { pay: Payment ->
            activity?.run {

                d("등록하니?")

                viewModel.payment().observe(this, EventObserver(this@IamPortWebViewMode::requestPayment))

                viewModel.openWebView().observe(this, EventObserver(this@IamPortWebViewMode::openWebView))
                viewModel.niceTransRequestParam().observe(this, EventObserver(this@IamPortWebViewMode::openNiceTransApp))
                viewModel.thirdPartyUri().observe(this, EventObserver(this@IamPortWebViewMode::openThirdPartyApp))

                viewModel.impResponse().observe(this, EventObserver(this@IamPortWebViewMode::sdkFinish))

                viewModel.startPayment(pay)
            }
        }
    }

    /**
     * 결제 요청 실행
     */
    override fun requestPayment(it: Payment) {
        d("나왔니??")
        activity?.run {
            if (!Util.isInternetAvailable(this)) {
                sdkFinish(IamPortResponse.makeFail(it, msg = "네트워크 연결 안됨"))
                return
            }
        }
        viewModel.requestPayment(it)
    }


    private val backPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            webview?.run {
                if (canGoBack()) {
                    goBack()
                } else {
                    activity?.onBackPressed()
                }
            }
        }
    }

    override fun onBackPressed() {
        activity?.run {
            activity?.onBackPressedDispatcher?.addCallback(this, backPressCallback)
        }
    }

    /**
     * 모든 결과 처리 및 SDK 종료
     */
    override fun sdkFinish(iamPortResponse: IamPortResponse?) {
        i("call sdkFinish")
        d("sdkFinish => ${iamPortResponse.toString()}")
        Iamport.callback.invoke(iamPortResponse)
    }

    /**
     * 뱅크페이 외부앱 열기 for nice PG + 실시간계좌이체(trans)
     */
    override fun openNiceTransApp(it: String) {
        runCatching {
            launcherBankPay?.launch(it) // 뱅크페이 앱 실행
        }.onFailure {
            // 뱅크페이 앱 패키지는 하드코딩
            activity?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Util.getMarketId(ProvidePgPkg.BANKPAY.pkg))))
        }
    }


    /**
     * 외부앱 열기
     */
    override fun openThirdPartyApp(it: Uri) {
        d("openThirdPartyApp $it")
        Intent.parseUri(it.toString(), Intent.URI_INTENT_SCHEME)?.let { intent: Intent ->
            runCatching {
                activity?.startActivity(intent)
            }.onFailure {
                movePlayStore(intent)
            }
        }
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
                else -> providePgPkg.pkg
            }
        }

        if (!pkg.isNullOrBlank()) {
            d("movePlayStore :: $pkg")
            activity?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Util.getMarketId(pkg))))
        }
    }


    /**
     * 웹뷰 오픈
     */
    override fun openWebView(payment: Payment) {
        d("오픈! 웹뷰")

        val evaluateJS = fun(jsMethod: String) {
            val js = "javascript:$jsMethod"
            d("evaluateJS => $js")
            launch {
                webview?.run {
                    this.loadUrl(js)
                }
            }
        }

        webview?.run {
            fitsSystemWindows = true
            settingsWebView(this)
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            clearCache(true)
            addJavascriptInterface(
                JsNativeInterface(payment, get(named("${CONST.KOIN_KEY}Gson")), get(), evaluateJS),
                CONST.PAYMENT_WEBVIEW_JS_INTERFACE_NAME
            )
            webViewClient = viewModel.getWebViewClient(payment)
            visibility = View.VISIBLE

            loadUrl(CONST.PAYMENT_FILE_URL) // load WebView
            webChromeClient = IamportWebChromeClient()
        }
    }

}