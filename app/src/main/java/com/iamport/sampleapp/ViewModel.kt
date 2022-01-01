package com.iamport.sampleapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iamport.sdk.data.sdk.*
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.utils.Event
import java.util.*

class ViewModel : ViewModel() {

    lateinit var pg: PG
    lateinit var payMethod: PayMethod
    var userCode: String = ""
    var paymentName: String = ""
    var merchantUid: String = ""
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
        val card = if (cardDirectCode.isNotEmpty()) Card(Direct(code = cardDirectCode)) else null

        return IamPortRequest(
            pg = pg.makePgRawName(pgId = ""),           // PG 사
            pay_method = payMethod.name,                // 결제수단
            name = paymentName,                         // 주문명
            merchant_uid = merchantUid,                 // 주문번호
            amount = amount,                            // 결제금액
            buyer_name = "남궁안녕",
            card = card, // 카드사 다이렉트
            custom_data = """
                {
                  "employees": {
                    "employee": [
                      {
                        "id": "1",
                        "firstName": "Tom",
                        "lastName": "Cruise",
                        "photo": "https://jsonformatter.org/img/tom-cruise.jpg",
                        "cuppingnote": "[\"일\",\"이\",\"삼\",\"사\",\"오\",\"육\",\"칠\"]"
                      },
                      {
                        "id": "2",
                        "firstName": "Maria",
                        "lastName": "Sharapova",
                        "photo": "https://jsonformatter.org/img/Maria-Sharapova.jpg"
                      },
                      {
                        "id": "3",
                        "firstName": "Robert",
                        "lastName": "Downey Jr.",
                        "photo": "https://jsonformatter.org/img/Robert-Downey-Jr.jpg"
                      }
                    ]
                  }
                }
            """.trimIndent()
//            customer_uid = getRandomCustomerUid() // 정기결제
        )
    }

    private fun getRandomCustomerUid(): String {
        return "mcuid_aos_${Date().time}"
    }

}