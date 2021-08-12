package com.iamport.sdk.domain.strategy.webview

import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.annotation.RequiresApi
import com.iamport.sdk.data.nice.BankPayResultCode
import com.iamport.sdk.domain.utils.Event
import com.orhanobut.logger.Logger

open class IamPortMobileModeWebViewClient : WebViewStrategy() {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {

        request?.url?.let {
//            bus.changeUrl.postValue(Event(it))
            bus.changeUrl.value = (Event(it))
        }

        return super.shouldOverrideUrlLoading(view, request)
    }
}