package com.iamport.sdk

import com.iamport.sdk.data.chai.request.PrepareRequest
import com.iamport.sdk.data.remote.ApiHelper
import com.iamport.sdk.data.remote.ChaiApi
import com.iamport.sdk.data.remote.IamportApi
import com.iamport.sdk.data.remote.ResultWrapper
import com.iamport.sdk.data.sdk.IamPortRequest
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.PayMethod
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.repository.StrategyRepository
import com.iamport.sdk.domain.strategy.base.JudgeStrategy
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.Util
import com.iamport.sdk.presentation.viewmodel.MainViewModel
import com.orhanobut.logger.Logger.i
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.koin.test.inject


class SdkUnitTest : AbstractKoinTest() {

    private val viewModel: MainViewModel by inject()
    private val iamportApi: IamportApi by inject()
    private val chaiApi: ChaiApi by inject()
    private val repository: StrategyRepository by inject()


    private suspend fun getUsers(userCode: String) {
        ApiHelper.safeApiCall(Dispatchers.IO) { iamportApi.getUsers(userCode) }.let {
            when (it) {
                is ResultWrapper.Success -> {
                    val method = repository.judgeStrategy.javaClass.getDeclaredMethod("findDefaultUserData", ArrayList::class.java)
                    method.isAccessible = true
                    i("userCode :: $userCode, default PG :: ${method.invoke(repository.judgeStrategy, it.value.data)}")
                    assertThat(true, `is`(true))
                }
                is ResultWrapper.GenericError -> {
                    i("${it.code} ${it.error}")
                    assertThat(false, `is`(true))
                }
                is ResultWrapper.NetworkError -> {
                    i(" ${it.error}")
                    assertThat(false, `is`(true))
                }
            }
        }
    }

    private fun getDefaultPayment(): Payment {
        val userCode = "12345"
        val request = IamPortRequest(
            pg = PG.kcp.makePgRawName(),
            pay_method = PayMethod.card.name,
            name = "주문명001",
            merchant_uid = "주문번호001",
            amount = "1000",
            buyer_name = "남궁안녕"
        )

        return Payment(userCode, iamPortRequest = request)
    }


    @Test
    fun `remote 개발 유저 코드 통신 테스트`() = runBlocking {
        Util.DevUserCode.values().forEach {
            delay(150)
            getUsers(it.name)
        }
    }

    @Test
    fun `remote 샘플 유저 코드 통신 테스트`() = runBlocking {

        Util.SampleUserCode.values().forEach {
            delay(150)
            getUsers(it.name)
        }
    }


    @Test
    fun `remote getUsers 차이 판별 테스트`() = runBlocking {

        val chaiPayment = getDefaultPayment().run {
            copy(
                userCode = Util.SampleUserCode.imp37739582.name,
                iamPortRequest = iamPortRequest?.copy(
                    pg = PG.chai.makePgRawName(),
                    pay_method = PayMethod.trans.name,
                )
            )
        }

        val judge = repository.judgeStrategy.judge(chaiPayment)
        assertThat(judge.first, `is`(JudgeStrategy.JudgeKinds.CHAI))
    }

    @Test
    fun `remote 차이 prepare 테스트`() = runBlocking {

        val chaiId = "15ef5fc6-43c8-4a27-b29f-e4a13897fc4c"
        val chaiPayment = getDefaultPayment().run {
            copy(
                userCode = Util.SampleUserCode.imp37739582.name,
                iamPortRequest = iamPortRequest?.copy(
                    pg = PG.chai.makePgRawName(),
                    pay_method = PayMethod.trans.name,
                )
            )
        }

        PrepareRequest.make(chaiId, chaiPayment)?.let {
            when (val resonse = ApiHelper.safeApiCall(Dispatchers.IO) { iamportApi.postPrepare(it) }) {
                is ResultWrapper.Success -> {
                    i("${resonse.value}")
                    assertThat(true, `is`(true))
                }
                else -> assertThat(false, `is`(true))
            }
        }
    }


    @Test
    fun `가상계좌 유효성 실패 검증`() = runBlocking {
        val payment = getDefaultPayment().run {
            copy(
                iamPortRequest = iamPortRequest?.copy(
                    pg = PG.kcp.makePgRawName(),
                    pay_method = PayMethod.vbank.name,
                )
            )
        }
        i("$payment")

        Payment.validator(payment).run {
            i("$second")
            assertThat(first, `is`(false))
            assertThat(second, `is`(CONST.ERR_PAYMENT_VALIDATOR_VBANK))
        }
    }

    @Test
    fun `가상계좌 유효성 검증`() = runBlocking {

        val payment = getDefaultPayment().run {
            copy(
                iamPortRequest = iamPortRequest?.copy(
                    pg = PG.kcp.makePgRawName(),
                    pay_method = PayMethod.vbank.name,
                    vbank_due = "2020121211302",
                )
            )
        }
        i("$payment")

        Payment.validator(payment).run {
            i("$second")
            assertThat(first, `is`(true))
        }
    }

    @Test
    fun `휴대폰 소액결제 유효성 실패 검증`() = runBlocking {

        val payment = getDefaultPayment().run {
            copy(
                iamPortRequest = iamPortRequest?.copy(
                    pg = PG.kcp.makePgRawName(),
                    pay_method = PayMethod.phone.name,
                )
            )
        }
        i("$payment")

        Payment.validator(payment).run {
            i("$second")
            assertThat(first, `is`(false))
            assertThat(second, `is`(CONST.ERR_PAYMENT_VALIDATOR_PHONE))
        }
    }

    @Test
    fun `휴대폰 소액결제 유효성 검증`() = runBlocking {

        val payment = getDefaultPayment().run {
            copy(
                iamPortRequest = iamPortRequest?.copy(
                    pg = PG.kcp.makePgRawName(),
                    pay_method = PayMethod.phone.name,
                    digital = true,
                )
            )
        }
        i("$payment")

        Payment.validator(payment).run {
            i("$second")
            assertThat(first, `is`(true))
        }
    }

    @Test
    fun `다날 - 가상계좌 실패 검증`() = runBlocking {

        val payment = getDefaultPayment().run {
            copy(
                iamPortRequest = iamPortRequest?.copy(
                    pg = PG.danal_tpay.makePgRawName(),
                    pay_method = PayMethod.vbank.name,
                    vbank_due = "2020121211302",
                )
            )
        }
        i("$payment")

        Payment.validator(payment).run {
            i("$second")
            assertThat(first, `is`(false))
            assertThat(second, `is`(CONST.ERR_PAYMENT_VALIDATOR_DANAL_VBANK))
        }
    }

    @Test
    fun `다날 - 가상계좌 유효성 검증`() = runBlocking {

        val payment = getDefaultPayment().run {
            copy(
                iamPortRequest = iamPortRequest?.copy(
                    pg = PG.danal_tpay.makePgRawName(),
                    pay_method = PayMethod.vbank.name,
                    vbank_due = "2020121211302",
                    biz_num = "1234567890"
                )
            )
        }
        i("$payment")

        Payment.validator(payment).run {
            i("$second")
            assertThat(first, `is`(true))
        }
    }

//    @Test
//    fun `페이팔 결제 실패 검증`() = runBlocking {
//
//        val payment = getDefaultPayment().run {
//            copy(
//                iamPortRequest = iamPortRequest?.copy(
//                    pg = PG.paypal.makePgRawName(),
//                    pay_method = PayMethod.card,
//                )
//            )
//        }
//        i("$payment")
//
//        Payment.validator(payment).run {
//            i("$second")
//            assertThat(first, `is`(false))
//            assertThat(second, `is`(CONST.ERR_PAYMENT_VALIDATOR_PAYPAL))
//        }
//    }

    @Test
    fun `페이팔 결제 유효성 검증`() = runBlocking {

        val payment = getDefaultPayment().run {
            copy(
                iamPortRequest = iamPortRequest?.copy(
                    pg = PG.paypal.makePgRawName(),
                    pay_method = PayMethod.card.name,
                    m_redirect_url = CONST.IAMPORT_PROD_URL,
                )
            )
        }
        i("$payment")

        Payment.validator(payment).run {
            i("$second")
            assertThat(first, `is`(true))
        }
    }
}


