package com.iamport.sampleapp

import android.util.Log
import android.webkit.JsResult
import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.iamport.sdk.domain.IamportWebChromeClient
import com.iamport.sdk.domain.strategy.webview.IamPortMobileModeWebViewClient

open class MyWebViewClient : IamPortMobileModeWebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {

        Log.i("MyWebViewClient", "updated webview url ${view?.url}")

        return super.shouldOverrideUrlLoading(view, request)
    }

}

open class MyWebViewChromeClient : IamportWebChromeClient() {

    override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean {
        Log.i("MyWebViewChromeClient", "called this function")
        return super.onJsConfirm(view, url, message, result)
    }
}

