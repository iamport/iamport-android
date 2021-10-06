package com.iamport.sdk.domain.repository

import android.webkit.WebViewClient
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.PayMethod
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.di.IamportKoinComponent
import com.iamport.sdk.domain.strategy.base.IStrategy
import com.iamport.sdk.domain.strategy.base.JudgeStrategy
import com.iamport.sdk.domain.strategy.chai.ChaiStrategy
import com.iamport.sdk.domain.strategy.webview.IamPortMobileModeWebViewClient
import com.iamport.sdk.domain.strategy.webview.WebViewStrategy
import com.orhanobut.logger.Logger
import org.koin.core.component.inject

class StrategyRepository : IamportKoinComponent {

    val judgeStrategy: JudgeStrategy by inject()
    val chaiStrategy: ChaiStrategy by inject() // 결제 중 BG 폴링하는 차이 전략
    var mobileWebModeStrategy: IamPortMobileModeWebViewClient? = null


    private val webViewStrategy: WebViewStrategy by inject() // webview 사용하는 pg

//    private val niceTransWebViewStrategy: NiceTransWebViewStrategy by inject()

    /**
     * 실제로 앱 띄울 결제 타입
     */
    enum class PaymentKinds {
        CHAI, NICE, WEB
    }

    fun init() {
//        chaiStrategy.init()
        mobileWebModeStrategy = null
    }

    fun failSdkFinish(payment: Payment) {
        when (getPaymentKinds(payment)) {
            PaymentKinds.CHAI -> {
//                chaiStrategy.failFinish("사용자가 결제확인 서비스를 종료하셨습니다")
                Logger.i("사용자가 결제확인 서비스를 종료하셨습니다")
                chaiStrategy.init()
            }
            else -> {
                // 사실상 호출될 일이 없겠지만 추가
                webViewStrategy.failureFinish(payment, null, "사용자가 결제확인 서비스를 종료하셨습니다 payment [$payment]")
            }
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
                Pair(it, PayMethod.from(request.pay_method)).let { pair: Pair<PG, PayMethod> ->
                    return when {
                        isChaiPayment(pair) -> PaymentKinds.CHAI
                        isNiceTransPayment(pair) -> PaymentKinds.WEB // PaymentKinds.NICE 사용 안함
                        else -> PaymentKinds.WEB
                    }
                }
            } ?: run { return PaymentKinds.WEB } // default WEB
        } ?: run { return PaymentKinds.WEB } // default WEB
    }

    // for 결제요청
    fun getWebViewStrategy(): IStrategy {
        return webViewStrategy
//        return when (getPaymentKinds(payment)) {
//            PaymentKinds.NICE -> niceTransWebViewStrategy
//            else -> webViewStrategy
//        }
    }

    // for webview mode inject
    fun getWebViewClient(): WebViewClient {
        return webViewStrategy
//        return when (getPaymentKinds(payment)) {
//            PaymentKinds.NICE -> niceTransWebViewStrategy
//            else -> webViewStrategy
//        }
    }

//    fun getNiceTransWebViewClient(): NiceTransWebViewStrategy {
//        return niceTransWebViewStrategy
//    }

    fun getMobileWebModeClient(): IamPortMobileModeWebViewClient {
        return mobileWebModeStrategy ?: run {
            mobileWebModeStrategy = IamPortMobileModeWebViewClient()
            mobileWebModeStrategy as IamPortMobileModeWebViewClient
        }
    }

    fun updateMobileWebModeClient(client: IamPortMobileModeWebViewClient) {
        mobileWebModeStrategy = client
    }


}