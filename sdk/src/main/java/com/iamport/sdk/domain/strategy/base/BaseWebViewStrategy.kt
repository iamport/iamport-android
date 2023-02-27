package com.iamport.sdk.domain.strategy.base

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.*
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.iamport.sdk.data.sdk.IamportResponse
import com.iamport.sdk.data.sdk.IamportRequest
import com.iamport.sdk.domain.utils.Constant
import com.iamport.sdk.domain.utils.Event
import com.iamport.sdk.domain.utils.WebViewLiveDataEventBus
import com.orhanobut.logger.Logger
import com.orhanobut.logger.Logger.d

open class BaseWebViewStrategy : WebViewClient(), IStrategy {

    protected val gson: Gson by lazy { Gson() }
    protected val bus: WebViewLiveDataEventBus by lazy { WebViewLiveDataEventBus }

    lateinit var request: IamportRequest

    override fun init() {}

    override suspend fun doWork(request: IamportRequest) {
        super.doWork(request)
        d("doWork! $request")
        this.request = request
    }

    /**
     * SDK 종료
     */
    override fun sdkFinish(response: IamportResponse?) {
        bus.impResponse.postValue(Event(response))
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
    }

    override fun onPageFinished(view: WebView, url: String) {
        d(url)
//        if (url != Constant.PAYMENT_FILE_URL) {
        bus.loading.postValue(Event(false))
//        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        Logger.w("onReceivedError code ${error?.errorCode}, description ${error?.description}")
        super.onReceivedError(view, request, error)
        // 에러 발생시, 결제 취소 페이지 이동
//        failureFinish(payment, msg = "code ${error?.errorCode}, description ${error?.description}")
    }


    // WebView 리퀘스트 정보 보기
    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            d("url :: ${request?.url?.toString()}")
//            d("requestHeaders :: ${request?.requestHeaders.toString()}")
//            d("method :: ${request?.method.toString()}")
//        }
        return super.shouldInterceptRequest(view, request)
    }

    /**
     * 성공해서 SDK 종료
     */
    protected fun successFinish(request: IamportRequest) {
        successFinish(request, msg = Constant.EMPTY_STR)
    }

    /**
     * 앱 uri 인지 여부
     */
    protected fun isAppUrl(uri: Uri): Boolean {
        return uri.scheme.let {
            it != Constant.HTTP_SCHEME && it != Constant.HTTPS_SCHEME && it != Constant.ABOUT_BLANK_SCHEME
        }
    }

    /**
     * 결제 끝났는지 여부
     */
    protected fun isPaymentOver(uri: Uri): Boolean {
        return uri.toString().contains(Constant.IAMPORT_DETECT_URL)
    }

}