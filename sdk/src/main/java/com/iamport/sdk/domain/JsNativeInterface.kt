package com.iamport.sdk.domain

import android.util.Base64
import android.webkit.JavascriptInterface
import com.google.gson.Gson
import com.iamport.sdk.data.sdk.IamPortCertification
import com.iamport.sdk.data.sdk.IamPortRequest
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.data.sdk.Payment.STATUS.*
import com.iamport.sdk.domain.di.IamportKoinComponent
import com.iamport.sdk.domain.utils.Event
import com.iamport.sdk.domain.utils.WebViewLiveDataEventBus
import com.orhanobut.logger.Logger

class JsNativeInterface(val payment: Payment, val gson: Gson, val evaluateJS: ((String) -> Unit)) : IamportKoinComponent {
    private val bus: WebViewLiveDataEventBus by lazy { WebViewLiveDataEventBus }

    /**
     * 아임포트 JS SDK 에서 콜백 호출시에 해당 함수 동작
     */
    @JavascriptInterface
    fun customCallback(response: String) {
        Logger.d("customCallback response :: $response")
        runCatching {
            val impRes = gson.fromJson(response, IamPortResponse::class.java)
            Logger.d("customCallback paymentover :: $impRes")
            bus.impResponse.postValue(Event(impRes))
        }
    }


    /**
     * 아임포트 JS SDK 에서 콜백 호출시에 해당 함수 동작
     */
    @JavascriptInterface
    fun debugConsoleLog(logStr: String) {
        Logger.d("WebViewConsoleLog => $logStr")
    }


    /**
     * 아임포트 JS SDK 에서 콜백 호출시에 해당 함수 동작
     */
    @JavascriptInterface
    fun startWorkingSdk() {
        Logger.d("JS SDK 통한 결제 시작 요청")

        initSDK(payment.userCode, payment.tierCode)

        when (payment.getStatus()) {
            PAYMENT -> payment.iamPortRequest?.let {
                requestPay(it)
            }
            CERT -> payment.iamPortCertification?.let {
                certification(it)
            }
            ERROR -> {
                IamPortResponse.makeFail(payment, msg = "payment status ERROR").let {
                    bus.impResponse.postValue(Event(it))
                }
            }
        }

    }

    private fun initSDK(userCode: String, tierCode: String? = null) {
        Logger.d("userCode : '${userCode}', tierCode : '${tierCode}'")

        val jsInitMethod = tierCode?.run {
            "agency('${userCode}', '${tierCode}');" // IMP.agency
        } ?: run {
            "init('${userCode}');" // IMP.init
        }

        evaluateJS(jsInitMethod)
    }

    private fun requestPay(request: IamPortRequest) {
        if (request.custom_data.isNullOrEmpty()) {
            requestPayNormal(request)
        } else {
            requestPayWithCustomData(request, request.custom_data)
        }
    }

    private fun requestPayNormal(request: IamPortRequest) {
        Logger.d(request)
        evaluateJS("requestPay('${gson.toJson(request)}');")
    }

    private fun requestPayWithCustomData(request: IamPortRequest, customData: String) {
        Logger.d(request)

        val encodedString: String = Base64.encodeToString(customData.toByteArray(), Base64.NO_WRAP)
        evaluateJS("requestPayWithCustomData('${gson.toJson(request)}', '${encodedString}');")
    }

    private fun certification(certification: IamPortCertification) {
        Logger.d(certification)
        evaluateJS("certification('${gson.toJson(certification)}');")
    }

}