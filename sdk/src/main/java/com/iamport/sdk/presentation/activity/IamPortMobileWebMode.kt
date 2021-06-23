package com.iamport.sdk.presentation.activity

import android.view.View
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.IamportWebChromeClient
import com.iamport.sdk.domain.utils.EventObserver
import com.orhanobut.logger.Logger
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
open class IamPortMobileWebMode(bankPayLauncher: ActivityResultLauncher<String>?) : IamPortWebViewMode(bankPayLauncher = bankPayLauncher) {

    fun initStart(activity: ComponentActivity, webview: WebView) {
        Logger.i("HELLO I'MPORT Mobile Web Mode SDK!")

        this.activity = activity
        this.webview = webview

        observeViewModel(null) // 관찰할 LiveData
    }


    /**
     * 관찰할 LiveData 옵저빙
     */
    override fun observeViewModel(payment: Payment?) {
        activity?.run {

            viewModel.niceTransRequestParam().observe(this, EventObserver(this@IamPortMobileWebMode::openNiceTransApp))
            viewModel.thirdPartyUri().observe(this, EventObserver(this@IamPortMobileWebMode::openThirdPartyApp))
            viewModel.impResponse().observe(this, EventObserver(this@IamPortMobileWebMode::sdkFinish))

            openWebView()
        }
    }


    override fun sdkFinish(iamPortResponse: IamPortResponse?) {
        // ignore
        Logger.d("sdkFinish MobileWebMode => $iamPortResponse")
    }

    private fun openWebView() {
        webview?.run {
            fitsSystemWindows = true
            settingsWebView(this)
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            clearCache(true) // FIXME: 안지워도 될까? 고민..

            webViewClient = viewModel.getNiceTransWebViewClient()
            visibility = View.VISIBLE

            webChromeClient = IamportWebChromeClient()
        }
    }

}