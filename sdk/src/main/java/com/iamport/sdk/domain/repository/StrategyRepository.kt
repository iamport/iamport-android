package com.iamport.sdk.domain.repository

import android.webkit.WebViewClient
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.PayMethod
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.strategy.base.IStrategy
import com.iamport.sdk.domain.strategy.base.JudgeStrategy
import com.iamport.sdk.domain.strategy.chai.ChaiStrategy
import com.iamport.sdk.domain.strategy.webview.NiceTransWebViewStrategy
import com.iamport.sdk.domain.strategy.webview.WebViewStrategy
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class StrategyRepository : KoinComponent {

    val judgeStrategy: JudgeStrategy by inject() // 결제 중 BG 폴링하는 차이 전략
    val chaiStrategy: ChaiStrategy by inject() // 결제 중 BG 폴링하는 차이 전략

    private val webViewStrategy: WebViewStrategy by inject() // webview 사용하는 pg
    private val niceTransWebViewStrategy: NiceTransWebViewStrategy by inject() //

    /**
     * 실제로 앱 띄울 결제 타입
     */
    enum class PaymenyKinds {
        CHAI, NICE, WEB
    }

    /**
     * PG 와 PayMethod 로 결제 타입하여 가져옴
     * @return PaymenyKinds
     */
    private fun getPaymentKinds(payment: Payment): PaymenyKinds {

        fun isChaiPayment(pgPair: Pair<PG, PayMethod>): Boolean {
            return pgPair.first == PG.chai
        }

        fun isNiceTransPayment(pgPair: Pair<PG, PayMethod>): Boolean {
            return pgPair.first == PG.nice && pgPair.second == PayMethod.trans
        }

        val request = payment.iamPortRequest
        request.pgEnum?.let {
            Pair(it, request.pay_method).let { pair: Pair<PG, PayMethod> ->
                return when {
                    isChaiPayment(pair) -> PaymenyKinds.CHAI
                    isNiceTransPayment(pair) -> PaymenyKinds.NICE
                    else -> PaymenyKinds.WEB
                }
            }
        } ?: run { return PaymenyKinds.WEB } // default WEB
    }

    fun getWebViewStrategy(payment: Payment): IStrategy {
        return when (getPaymentKinds(payment)) {
            PaymenyKinds.NICE -> niceTransWebViewStrategy
            else -> webViewStrategy
        }
    }

    fun getWebViewClient(payment: Payment): WebViewClient {
        return when (getPaymentKinds(payment)) {
            PaymenyKinds.NICE -> niceTransWebViewStrategy
            else -> webViewStrategy
        }
    }

    fun getNiceTransWebViewClient(): NiceTransWebViewStrategy {
        return niceTransWebViewStrategy
    }
}