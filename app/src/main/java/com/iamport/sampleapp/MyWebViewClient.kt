package com.iamport.sampleapp

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.iamport.sdk.domain.strategy.webview.IamPortMobileModeWebViewClient

open class MyWebViewClient : IamPortMobileModeWebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {

        Log.i("MyWebViewClient", "updated webview url ${view?.url}")

        return super.shouldOverrideUrlLoading(view, request)
    }

}