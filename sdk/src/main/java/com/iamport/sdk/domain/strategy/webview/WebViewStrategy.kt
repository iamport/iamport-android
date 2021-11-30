package com.iamport.sdk.domain.strategy.webview

import android.net.Uri
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.annotation.RequiresApi
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.strategy.base.BaseWebViewStrategy
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.Event
import com.iamport.sdk.domain.utils.Util
import com.orhanobut.logger.Logger.d


open class WebViewStrategy : BaseWebViewStrategy() {

    override suspend fun doWork(payment: Payment) {
        super.doWork(payment)
        // 오픈 웹뷰!
        bus.openWebView.postValue(Event(payment))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        request?.url?.let {
            if (isPaymentOver(it)) {
                paymentOver(it)
            } else {
                super.onReceivedError(view, request, error)
            }
        } ?: run {
            super.onReceivedError(view, request, error)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {

        request?.url?.let {
            d(it)

            if (it.scheme == CONST.ABOUT_BLANK_SCHEME) {
                return true // 이동하지 않음
            }

            if (isAppUrl(it)) {
                bus.thirdPartyUri.postValue(Event(it))
                return true
            }

            if (isPaymentOver(it)) {
                paymentOver(it)
                return true
            }
        }

        return super.shouldOverrideUrlLoading(view, request)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun paymentOver(uri: Uri) {
        val response = Util.getQueryStringToImpResponse(uri, gson)
        d("paymentOver :: $response")
        sdkFinish(response)
    }


}