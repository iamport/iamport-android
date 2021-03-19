package com.iamport.sdk.domain.repository

import android.webkit.WebViewClient
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.PayMethod
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.di.IamportKoinComponent
import com.iamport.sdk.domain.strategy.base.IStrategy
import com.iamport.sdk.domain.strategy.base.JudgeStrategy
import com.iamport.sdk.domain.strategy.chai.ChaiStrategy
import com.iamport.sdk.domain.strategy.webview.CertificationWebViewStrategy
import com.iamport.sdk.domain.strategy.webview.NiceTransWebViewStrategy
import com.iamport.sdk.domain.strategy.webview.WebViewStrategy
import com.orhanobut.logger.Logger
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
class StrategyRepository : IamportKoinComponent {

    val judgeStrategy: JudgeStrategy by inject() // 결제 중 BG 폴링하는 차이 전략
    val chaiStrategy: ChaiStrategy by inject() // 결제 중 BG 폴링하는 차이 전략

    private val webViewStrategy: WebViewStrategy by inject() // webview 사용하는 pg
    private val niceTransWebViewStrategy: NiceTransWebViewStrategy by inject() //

    private val certificationWebViewStrategy: CertificationWebViewStrategy by inject() //

    /**
     * 실제로 앱 띄울 결제 타입
     */
    enum class PaymentKinds {
        CHAI, NICE, WEB
    }

    fun failSdkFinish(payment: Payment) {
        when (getPaymentKinds(payment)) {
            PaymentKinds.CHAI -> chaiStrategy.failFinish("사용자가 결제확인 서비스 종료하셨습니다")
            else -> Logger.d("사용자가 결제확인 서비스 종료하셨습니다")
        }
    }

    /**
     * PG 와 PayMethod 로 결제 타입하여 가져옴
     * @return PaymenyKinds
     */
    private fun getPaymentKinds(payment: Payment): PaymentKinds {

        fun isChaiPayment(pgPair: Pair<PG, PayMethod>): Boolean {
            return pgPair.first == PG.chai
        }

        fun isNiceTransPayment(pgPair: Pair<PG, PayMethod>): Boolean {
            return pgPair.first == PG.nice && pgPair.second == PayMethod.trans
        }

        payment.iamPortRequest?.let { request ->
            request.pgEnum?.let {
                Pair(it, request.pay_method).let { pair: Pair<PG, PayMethod> ->
                    return when {
                        isChaiPayment(pair) -> PaymentKinds.CHAI
                        isNiceTransPayment(pair) -> PaymentKinds.NICE
                        else -> PaymentKinds.WEB
                    }
                }
            } ?: run { return PaymentKinds.WEB } // default WEB
        } ?: run { return PaymentKinds.WEB } // default WEB
    }

    fun getWebViewStrategy(payment: Payment): IStrategy {
        return when (getPaymentKinds(payment)) {
            PaymentKinds.NICE -> niceTransWebViewStrategy
            else -> webViewStrategy
        }
    }

    fun getWebViewClient(payment: Payment): WebViewClient {
        return when (getPaymentKinds(payment)) {
            PaymentKinds.NICE -> niceTransWebViewStrategy
            else -> webViewStrategy
        }
    }

    fun getNiceTransWebViewClient(): NiceTransWebViewStrategy {
        return niceTransWebViewStrategy
    }

}