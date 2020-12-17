package com.iamport.sdk

import android.os.Build
import com.iamport.sdk.data.remote.ApiHelper
import com.iamport.sdk.data.remote.IamportApi
import com.iamport.sdk.data.remote.ResultWrapper
import com.iamport.sdk.data.sdk.IamPortRequest
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.PayMethod
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.repository.StrategyRepository
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.Util
import com.iamport.sdk.presentation.activity.IamportSdk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SdkUnitTest : AbstractKoin() {

    private val iamportApi: IamportApi by inject()
    private val repository: StrategyRepository by inject()

    private lateinit var iamportSdk: IamportSdk

    private suspend fun getUsers(userCode: String) {
        ApiHelper.safeApiCall(Dispatchers.IO) { iamportApi.getUsers(userCode) }.let {
            when (it) {
                is ResultWrapper.Success -> {

                    val method = repository.judgeStrategy.javaClass.getDeclaredMethod("findDefaultUserData", ArrayList::class.java)
                    method.isAccessible = true
                    println("userCode :: $userCode, default PG :: ${method.invoke(repository.judgeStrategy, it.value.data)}")
                    assertThat(true, `is`(true))
                }
                is ResultWrapper.GenericError ->
                    assertThat(false, `is`(true))
                is ResultWrapper.NetworkError ->
                    assertThat(false, `is`(true))
            }
        }
    }

    @Test
    fun `개발 유저 코드 통신 테스트`() = runBlocking {
        Util.DevUserCode.values().forEach {
            delay(150)
            getUsers(it.name)
        }
    }

    @Test
    fun `샘플 유저 코드 통신 테스트`() = runBlocking {
        Util.SampleUserCode.values().forEach {
            delay(150)
            getUsers(it.name)
        }
    }

    private fun getDefaultPayment(): Payment {
        val userCode = "12345"
        val request = IamPortRequest(
            pg = PG.kcp.getPgSting(),
            pay_method = PayMethod.card,
            name = "주문명001",
            merchant_uid = "주문번호001",
            amount = "1000",
            buyer_name = "남궁안녕"
        )

        return Payment(userCode, request)
    }

    @Test
    fun `가상계좌 유효성 실패 검증`() = runBlocking {
        val payment = getDefaultPayment().run {
            copy(
                iamPortRequest = iamPortRequest.copy(
                    pg = PG.kcp.getPgSting(),
                    pay_method = PayMethod.vbank,
                )
            )
        }
        println(payment)

        Payment.validator(payment).run {
            println("$second")
            assertThat(first, `is`(false))
        }
    }

    @Test
    fun `가상계좌 유효성 검증`() = runBlocking {

        val payment = getDefaultPayment().run {
            copy(
                iamPortRequest = iamPortRequest.copy(
                    pg = PG.kcp.getPgSting(),
                    pay_method = PayMethod.vbank,
                    vbank_due = "2020121211302",
                )
            )
        }
        println(payment)

        Payment.validator(payment).run {
            println("$second")
            assertThat(first, `is`(true))
        }
    }

    @Test
    fun `휴대폰 소액결제 유효성 실패 검증`() = runBlocking {

        val payment = getDefaultPayment().run {
            copy(
                iamPortRequest = iamPortRequest.copy(
                    pg = PG.kcp.getPgSting(),
                    pay_method = PayMethod.phone,
                )
            )
        }
        println(payment)

        Payment.validator(payment).run {
            println("$second")
            assertThat(first, `is`(false))
        }
    }

    @Test
    fun `휴대폰 소액결제 유효성 검증`() = runBlocking {

        val payment = getDefaultPayment().run {
            copy(
                iamPortRequest = iamPortRequest.copy(
                    pg = PG.kcp.getPgSting(),
                    pay_method = PayMethod.phone,
                    digital = true,
                )
            )
        }
        println(payment)

        Payment.validator(payment).run {
            println("$second")
            assertThat(first, `is`(true))
        }
    }

    @Test
    fun `다날 - 가상계좌 실패 검증`() = runBlocking {

        val payment = getDefaultPayment().run {
            copy(
                iamPortRequest = iamPortRequest.copy(
                    pg = PG.danal_tpay.getPgSting(),
                    pay_method = PayMethod.vbank,
                )
            )
        }
        println(payment)

        Payment.validator(payment).run {
            println("$second")
            assertThat(first, `is`(false))
        }
    }

    @Test
    fun `다날 - 가상계좌 유효성 검증`() = runBlocking {

        val payment = getDefaultPayment().run {
            copy(
                iamPortRequest = iamPortRequest.copy(
                    pg = PG.danal_tpay.getPgSting(),
                    pay_method = PayMethod.vbank,
                    vbank_due = "2020121211302",
                )
            )
        }
        println(payment)

        Payment.validator(payment).run {
            println("$second")
            assertThat(first, `is`(true))
        }
    }

    @Test
    fun `페이팔 결제 실패 검증`() = runBlocking {

        val payment = getDefaultPayment().run {
            copy(
                iamPortRequest = iamPortRequest.copy(
                    pg = PG.paypal.getPgSting(),
                    pay_method = PayMethod.card,
                )
            )
        }
        println(payment)

        Payment.validator(payment).run {
            println("$second")
            assertThat(first, `is`(false))
        }
    }

    @Test
    fun `페이팔 결제 유효성 검증`() = runBlocking {

        val payment = getDefaultPayment().run {
            copy(
                iamPortRequest = iamPortRequest.copy(
                    pg = PG.paypal.getPgSting(),
                    pay_method = PayMethod.card,
                    m_redirect_url = CONST.IAMPORT_PROD_URL,
                )
            )
        }
        println(payment)

        Payment.validator(payment).run {
            println("$second")
            assertThat(first, `is`(true))
        }
    }
}


