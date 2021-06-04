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
open class IamportMobileWebMode(bankPayLauncher: ActivityResultLauncher<String>?) : IamPortWebViewMode(bankPayLauncher = bankPayLauncher) {

    fun initStart(activity: ComponentActivity, webview: WebView) {
        Logger.i("HELLO I'MPORT Mobile Web Mode SDK!")

        this.activity = activity
        this.webview = webview

//        onBackPressed()
        observeViewModel(null) // 관찰할 LiveData
    }


    /**
     * 관찰할 LiveData 옵저빙
     */
    override fun observeViewModel(payment: Payment?) {
        activity?.run {

            viewModel.niceTransRequestParam().observe(this, EventObserver(this@IamportMobileWebMode::openNiceTransApp))
            viewModel.thirdPartyUri().observe(this, EventObserver(this@IamportMobileWebMode::openThirdPartyApp))
            viewModel.impResponse().observe(this, EventObserver(this@IamportMobileWebMode::sdkFinish))

            openWebView()
        }
    }


    // 여기서만 처리하면 됨
    override fun sdkFinish(iamPortResponse: IamPortResponse?) {
        // ignore
    }

    private fun openWebView() {
        webview?.run {
            fitsSystemWindows = true
            settingsWebView(this)
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            clearCache(true)

            webViewClient = viewModel.getNiceTransWebViewClient()
            visibility = View.VISIBLE

            webChromeClient = IamportWebChromeClient()
        }
    }

}