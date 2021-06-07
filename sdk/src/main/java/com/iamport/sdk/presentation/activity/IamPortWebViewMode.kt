package com.iamport.sdk.presentation.activity

import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import com.google.gson.GsonBuilder
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.data.sdk.ProvidePgPkg
import com.iamport.sdk.domain.IamportWebChromeClient
import com.iamport.sdk.domain.JsNativeInterface
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.core.IamportLifecycleObserver
import com.iamport.sdk.domain.di.IamportKoinComponent
import com.iamport.sdk.domain.strategy.webview.NiceTransWebViewStrategy
import com.iamport.sdk.domain.utils.*
import com.iamport.sdk.presentation.contract.BankPayContract
import com.iamport.sdk.presentation.viewmodel.WebViewModel
import com.orhanobut.logger.Logger.*
import kotlinx.coroutines.*
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.get
import org.koin.core.qualifier.named


@KoinApiExtension
open class IamPortWebViewMode @JvmOverloads constructor(
    val bankPayLauncher: ActivityResultLauncher<String>?,
    scope: BaseCoroutineScope = UICoroutineScope()
) :
    IamportKoinComponent, BaseMain, BaseCoroutineScope by scope {

    val viewModel: WebViewModel = WebViewModel(get(), get())
//    private var bankPayLauncher: ActivityResultLauncher<String>? = null // 뱅크페이 앱 런처(for webview & mobile web mode)

    private var payment: Payment? = null
    var activity: ComponentActivity? = null
    var webview: WebView? = null


    /**
     * BaseActivity 에서 onCreate 시 호출
     */
    fun initStart(activity: ComponentActivity, webview: WebView, payment: Payment) {
        i("HELLO I'MPORT WebView SDK!")

        this.activity = activity
        this.payment = payment
        this.webview = webview

//        bankPayLauncher = activity.registerForActivityResult(BankPayContract()) {
//            viewModel.processBankPayPayment(it)
//        }

        onBackPressed()
        observeViewModel(payment) // 관찰할 LiveData
    }

    fun processBankPayPayment(resPair: Pair<String, String>) {
        viewModel.processBankPayPayment(resPair)
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

    fun close() {
        activity?.let {
            viewModel.payment().removeObservers(it)
            viewModel.openWebView().removeObservers(it)
            viewModel.niceTransRequestParam().removeObservers(it)
            viewModel.thirdPartyUri().removeObservers(it)
            viewModel.impResponse().removeObservers(it)
        }
        backPressCallback.remove()
        webview = null
        activity = null
    }

    private val backPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            webview?.run {
                if (canGoBack()) {
                    goBack()
                } else {
                    activity?.onBackPressed()
                }
            } ?: activity?.onBackPressed()
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
        d("openNiceTransApp $it")
        runCatching {
//            lifecycleObserver.bankPayLaunch(it) {
//                viewModel.processBankPayPayment(it)
//            }// 뱅크페이 앱 실행
            bankPayLauncher?.launch(it)
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
            }.recoverCatching {
                movePlayStore(intent)
            }.onFailure {
                i("설치 버튼을 이용하여 앱을 설치하세요.")
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
        d("오픈! 웹뷰 $payment")

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