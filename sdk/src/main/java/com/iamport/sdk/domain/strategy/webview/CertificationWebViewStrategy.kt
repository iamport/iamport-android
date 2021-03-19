package com.iamport.sdk.domain.strategy.webview

import android.net.Uri
import android.os.Build
import android.webkit.WebResourceError
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
open class CertificationWebViewStrategy : WebViewStrategy()