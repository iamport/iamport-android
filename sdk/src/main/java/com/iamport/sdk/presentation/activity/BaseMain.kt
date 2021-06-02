package com.iamport.sdk.presentation.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import com.iamport.sdk.BuildConfig
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment

interface BaseMain {
    fun openWebView(payment: Payment)
    fun openThirdPartyApp(it: Uri)
    fun openNiceTransApp(it: String)
    fun onBackPressed()
    fun observeViewModel(payment: Payment?)
    fun requestPayment(it: Payment)
    fun sdkFinish(iamPortResponse: IamPortResponse?)
    fun movePlayStore(intent: Intent)

    /**
     * 웹뷰 기본 세팅
     */
    fun settingsWebView(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(webView, true)
            }

            cacheMode = WebSettings.LOAD_NO_CACHE

            blockNetworkImage = false
            loadsImagesAutomatically = true

            if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = true  // api 26
            }

            useWideViewPort = false
            loadWithOverviewMode = true
            javaScriptCanOpenWindowsAutomatically = true

            domStorageEnabled = true
            loadWithOverviewMode = true
            allowContentAccess = true

            setSupportZoom(false)
            displayZoomControls = false
        }
    }
}