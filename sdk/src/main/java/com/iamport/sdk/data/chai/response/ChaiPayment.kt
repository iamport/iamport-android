package com.iamport.sdk.data.chai.response

// * status 값이 waiting → approved로 바뀌는지 polling
//* method : GET
//* 응답 content-type : application/json
//* URL : https://api.chai.finance/v1/payment/{Chai Payment Id}
//* 응답본문(Response) :
data class ChaiPayment(
    val paymentId: String, // "198ad2c1cc485629447c4527247c198bdb0cd82c",
    val type: String, // "payment",
    val status: String, // "waiting",
    val displayStatus: String, // "waiting",
    val idempotencyKey: String, // "CHAIINIpayTest20201027153612605769",
    val currency: String, // "KRW",
    val checkoutAmount: Int, // 1004,
    val discountAmount: Int, // 0,
    val billingAmount: Int, // 1004,
    val pointAmount: Int, // 0,
    val cashAmount: Int, // 0,
    val chargingAmount: Int, // 0,
    val cashbackAmount: Int, // 0,
    val taxFreeAmount: Int, // 0,
    val bookShowAmount: Int, // 0,
    val serviceFeeAmount: Int, // 0,
    val merchantDiscountAmount: Int, // 0,
    val merchantCashbackAmount: Int, // 0,
    val canceledAmount: Int, // 0,
    val canceledBillingAmount: Int, // 0,
    val canceledPointAmount: Int, // 0,
    val canceledCashAmount: Int, // 0,
    val canceledDiscountAmount: Int, // 0,
    val canceledCashbackAmount: Int, // 0,
    val returnUrl: String, // "https://ksmobile.inicis.com/smart/chaipayAcsResult.ini",
    val description: String, // "결제테스트",
    val cashbacks: ArrayList<String?>, // [], // TODO 이거 모르겠네
    val createdAt: String, // "2020-10-27T06:36:12.218Z",
    val updatedAt: String, // "2020-10-27T06:36:12.218Z",
    val metadata: PaymentMetadata,
)