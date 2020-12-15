package com.iamport.sdk.domain

import android.webkit.JavascriptInterface
import com.google.gson.Gson
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.utils.Event
import com.iamport.sdk.domain.utils.WebViewLiveDataEventBus
import com.orhanobut.logger.Logger
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@KoinApiExtension
class JsNativeInterface(val payment: Payment, val gson: Gson, private val bus: WebViewLiveDataEventBus) : KoinComponent {

    /**
     * 아임포트 JS SDK 에게 유저코드 전달§
     */
    @JavascriptInterface
    fun getUserCode(): String {
        return payment.userCode
    }

    /**
     * 아임포트 JS SDK 에게 iamPortRequest 객체(json) 전달
     */
    @JavascriptInterface
    fun getRequestParams(): String {
        return gson.toJson(payment.iamPortRequest)
    }

    /**
     * 아임포트 JS SDK 에서 콜백 호출시에 해당 함수 동작
     */
    @JavascriptInterface
    fun customCallback(response: String) {
        val impRes = gson.fromJson(response, IamPortResponse::class.java)
        Logger.d("customCallback paymentover :: $impRes")
        bus.impResponse.postValue(Event(impRes))
    }
}