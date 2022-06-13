package com.iamport.sampleapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iamport.sdk.data.sdk.IamPortRequest
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.PayMethod
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.utils.Event

class ViewModel : ViewModel() {

    lateinit var pg: PG
    lateinit var payMethod: PayMethod
    var userCode: String = ""
    var paymentName: String = ""
    var merchantUid: String = ""
    var customerUid: String = ""
    var pgMid: String = ""
    var amount: String = ""
    var cardDirectCode: String = ""

    val resultCallback = MutableLiveData<Event<IamPortResponse>>()
    override fun onCleared() {
        Iamport.close()
        super.onCleared()
    }

    /**
     * SDK 에 결제 요청할 데이터 구성
     */
    fun createIamPortRequest(): IamPortRequest {

        return IamPortRequest(
            // 토스 신모듈 테스트시, port-dev-live, port-dev-test, port-stg-live, port-stg-test, port-prod-live, port-prod-test
            // 예) pg.makePgRawName(pgId = "port-dev-live")
            pg = pg.makePgRawName(pgId = pgMid),           // PG 사
            pay_method = payMethod.name,                // 결제수단
            name = paymentName,                         // 주문명
            merchant_uid = merchantUid,                 // 주문번호
            amount = amount,                            // 결제금액
            buyer_name = "남궁안녕",
            customer_uid = customerUid // 정기결제
        )
    }

}