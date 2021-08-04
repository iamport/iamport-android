package com.iamport.sdk.domain.strategy.webview

import android.net.Uri
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.annotation.RequiresApi
import com.iamport.sdk.data.nice.BankPayResultCode
import com.iamport.sdk.data.nice.BankPayResultCode.*
import com.iamport.sdk.data.nice.NiceBankpay
import com.iamport.sdk.data.sdk.ProvidePgPkg
import com.iamport.sdk.domain.utils.Event
import com.orhanobut.logger.Logger.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
// 해당로직 쓰지 않아도 정상 결제 되는 듯 함
// NiceTransWebViewStrategy 가 필요없어진 듯
// bankpay launcher 도 삭제해도 될 듯
 */

open class NiceTransWebViewStrategy : WebViewStrategy() {

//    private val niceApi: NiceApi by inject()

    private var webView: WebView? = null
    private var bankTid: String = ""
    private var niceTransUrl: String = ""

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {

        webView = view

        request?.url?.let {
            d("아주 나이스~ $it")
//            if (isNiceTransScheme(it)) {
//
//                bankTid = it.getQueryParameter(NiceBankpay.USER_KEY).toString()
//                niceTransUrl = it.getQueryParameter(NiceBankpay.CALLBACKPARAM).toString()
//
//                makeBankPayData(it)?.let { data ->
//                    bus.niceTransRequestParam.postValue(Event(data)) // 뱅크페이 앱 열기
//                }
//                return true
//            }
        }

        return super.shouldOverrideUrlLoading(view, request)
    }

    /**
     * 뱅크페이 결제 결과 처리 (BankPayContract result)
     */
    fun processBankPayPayment(resPair: Pair<String, String>) {
        when (val code = BankPayResultCode.from(resPair.first)) {
            OK -> {
                d(BankPayResultCode.desc(code))
                webView?.postUrl(niceTransUrl, makeNiceTransPaymentsQuery(resPair).toByteArray())
            }
            CANCEL, FAIL_SIGN, FAIL_OTP,
            TIME_OUT, FAIL_CERT_MODULE_INIT -> {
                w(BankPayResultCode.desc(code))
                failureFinish(payment, msg = BankPayResultCode.desc(code))
            }
            else -> e("알 수 없는 에러 code : ${resPair.first}")
        }
    }

    private fun makeNiceTransPaymentsQuery(res: Pair<String, String>): String {
        return run {
            "${NiceBankpay.CALLBACKPARAM2}=${bankTid}" +
                    "&${NiceBankpay.CODE}=${res.first}" +
                    "&${NiceBankpay.VALUE}=${res.second}"
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun makeBankPayData(uri: Uri): String? {
        return URLDecoder.decode(
            uri.toString().substring(ProvidePgPkg.getNiceBankPayPrefix().length),
            StandardCharsets.UTF_8.toString()
        )
    }

    private fun isNiceTransScheme(uri: Uri): Boolean {
        return uri.scheme == ProvidePgPkg.BANKPAY.scheme
    }

}