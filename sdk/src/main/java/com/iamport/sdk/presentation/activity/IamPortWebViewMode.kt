package com.iamport.sdk.presentation.activity

import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.WebView
import androidx.activity.ComponentActivity
import com.google.gson.GsonBuilder
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.data.sdk.ProvidePgPkg
import com.iamport.sdk.domain.IamportWebChromeClient
import com.iamport.sdk.domain.JsNativeInterface
import com.iamport.sdk.domain.di.IamportKoinComponent
import com.iamport.sdk.domain.utils.*
import com.iamport.sdk.presentation.viewmodel.WebViewModel
import com.orhanobut.logger.Logger.*
import kotlinx.coroutines.*
import org.koin.core.component.get
import org.koin.core.qualifier.named


open class IamPortWebViewMode @JvmOverloads constructor(
    scope: BaseCoroutineScope = UICoroutineScope()
) : IamportKoinComponent, BaseMain, BaseCoroutineScope by scope {

    val viewModel: WebViewModel = WebViewModel(get())

    var activity: ComponentActivity? = null
    var webview: WebView? = null
    var paymentResultCallBack: ((IamPortResponse?) -> Unit)? = null

    /**
     * BaseActivity 에서 onCreate 시 호출
     */
    fun initStart(activity: ComponentActivity, webview: WebView, payment: Payment, paymentResultCallBack: ((IamPortResponse?) -> Unit)?) {
        i("HELLO I'MPORT WebView MODE SDK!")
        this.activity = activity
        this.webview = webview
        this.paymentResultCallBack = paymentResultCallBack
        observeViewModel(payment) // 관찰할 LiveData
    }

//    open fun processBankPayPayment(resPair: Pair<String, String>) {
//        viewModel.processBankPayPayment(resPair)
//    }

    /**
     * 관찰할 LiveData 옵저빙
     */
    override fun observeViewModel(payment: Payment?) {
        d(GsonBuilder().setPrettyPrinting().create().toJson(payment))
        payment?.let { pay: Payment ->
            activity?.let {
                viewModel.run {
                    openWebView().observe(it, EventObserver(this@IamPortWebViewMode::openWebView))
//                    niceTransRequestParam().observe(it, EventObserver(this@IamPortWebViewMode::openNiceTransApp))
                    thirdPartyUri().observe(it, EventObserver(this@IamPortWebViewMode::openThirdPartyApp))
                    impResponse().observe(it, EventObserver(this@IamPortWebViewMode::sdkFinish))

                    requestPayment(pay)
                }
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

    private fun removeObservers() {
        d("removeObservers")
        activity?.let {
            viewModel.run {
                d("do removeObservers")
                openWebView().removeObservers(it)
//                niceTransRequestParam().removeObservers(it)
                thirdPartyUri().removeObservers(it)
                impResponse().removeObservers(it)
            }
        }

        activity = null
    }

    fun close() {
        d("close WebViewMode")
        removeObservers()
        webview?.run {
            removeJavascriptInterface(CONST.PAYMENT_WEBVIEW_JS_INTERFACE_NAME)
            loadUrl("about:blank")
            removeAllViews()
            destroy()
        }
        webview = null
        paymentResultCallBack = null
    }


    /**
     * 모든 결과 처리 및 SDK 종료
     * IamportSdk 안건너고, 바로 콜백 호출하여 종료.
     */
    override fun sdkFinish(iamPortResponse: IamPortResponse?) {
        i("call sdkFinish")
        d("sdkFinish => ${iamPortResponse.toString()}")
        removeObservers()
        paymentResultCallBack?.invoke(iamPortResponse)
    }

    /**
     * 뱅크페이 외부앱 열기 for nice PG + 실시간계좌이체(trans)
     */
//    override fun openNiceTransApp(it: String) {
//        d("openNiceTransApp $it")
//        runCatching {
//            bankPayLauncher?.launch(it)
//        }.onFailure {
////             뱅크페이 앱 패키지는 하드코딩
//            activity?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Util.getMarketId(ProvidePgPkg.BANKPAY.pkg))))
//        }
//    }

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
                JsNativeInterface(payment, get(named("${CONST.KOIN_KEY}Gson")), evaluateJS),
                CONST.PAYMENT_WEBVIEW_JS_INTERFACE_NAME
            )
            webViewClient = viewModel.getWebViewClient()
            visibility = View.VISIBLE

            loadUrl(CONST.PAYMENT_FILE_URL) // load WebView
            webChromeClient = IamportWebChromeClient()
        } ?: run {
            e("웹뷰가 없엉..")
        }
    }

}