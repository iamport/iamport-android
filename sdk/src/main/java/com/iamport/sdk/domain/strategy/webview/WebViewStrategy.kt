package com.iamport.sdk.domain.strategy.webview

import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.annotation.RequiresApi
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.strategy.base.BaseWebViewStrategy
import com.iamport.sdk.domain.utils.Event
import com.iamport.sdk.domain.utils.Util
import com.orhanobut.logger.Logger.d
import org.koin.core.component.KoinApiExtension


@KoinApiExtension
open class WebViewStrategy : BaseWebViewStrategy() {

    override suspend fun doWork(payment: Payment) {
        super.doWork(payment)
        // 오픈 웹뷰!
        bus.openWebView.postValue(Event(payment))
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {

        request?.url?.let {
            d(it)

            if (isAppUrl(it)) {
                bus.thirdPartyUri.postValue(Event(it))
                return true
            }

            if (isPaymentOver(it)) {
                val response = Util.getQueryStringToImpResponse(it, gson)
                d("paymentOver :: $response")
                sdkFinish(response)
                return true
            }
        }

        return super.shouldOverrideUrlLoading(view, request)
    }
}