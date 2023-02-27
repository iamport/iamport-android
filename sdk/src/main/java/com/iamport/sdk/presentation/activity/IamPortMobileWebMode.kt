package com.iamport.sdk.presentation.activity

import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.lifecycle.LiveData
import com.iamport.sdk.data.sdk.IamportResponse
import com.iamport.sdk.data.sdk.IamportRequest
import com.iamport.sdk.domain.IamportWebChromeClient
import com.iamport.sdk.domain.strategy.webview.IamPortMobileModeWebViewClient
import com.iamport.sdk.domain.utils.Event
import com.iamport.sdk.domain.utils.EventObserver
import com.orhanobut.logger.Logger

open class IamportMobileWebMode() : IamportWebViewMode() {

    fun initStart(activity: ComponentActivity, webview: WebView) {
        Logger.i("HELLO I'MPORT Mobile Web Mode SDK!")

        this.activity = activity
        this.webview = webview

        observeViewModel(null) // 관찰할 LiveData
    }

//    override fun processBankPayPayment(resPair: Pair<String, String>) {
//        Logger.d("ignore processBankPayPayment")
//        viewModel.mobileModeProcessBankPayPayment(resPair)
//    }


    /**
     * 관찰할 LiveData 옵저빙
     */
    override fun observeViewModel(request: IamportRequest?) {
        activity?.run {

//            viewModel.niceTransRequestParam().observe(this, EventObserver(this@IamPortMobileWebMode::openNiceTransApp))
            viewModel.thirdPartyUri().observe(this, EventObserver(this@IamportMobileWebMode::openThirdPartyApp))
            viewModel.impResponse().observe(this, EventObserver(this@IamportMobileWebMode::sdkFinish))

            openWebView()
        }
    }


    fun detectShouldOverrideUrlLoading(): LiveData<Event<Uri>> {
        return viewModel.changeUrl()
    }

    override fun sdkFinish(iamPortResponse: IamportResponse?) {
        // ignore
        Logger.d("sdkFinish MobileWebMode => $iamPortResponse")
    }

    private fun openWebView() {
        webview?.run {
            fitsSystemWindows = true
            settingsWebView(this)
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            clearCache(true) // FIXME: 안지워도 될까? 고민..
            visibility = View.VISIBLE
//            webChromeClient = IamportWebChromeClient()

            webChromeClient = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                webChromeClient.let {
                    if (it is IamportWebChromeClient) {
                        return@let it
                    }
                    IamportWebChromeClient()
                }
            } else {
                IamportWebChromeClient()
            }

            webViewClient = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                webViewClient.let {
                    if (it is IamPortMobileModeWebViewClient) {
                        viewModel.updateMobileWebModeClient(client = it)
                        return@let it
                    }
                    viewModel.getMobileWebModeClient()
                }
            } else {
                viewModel.getMobileWebModeClient()
            }
        }
    }
}