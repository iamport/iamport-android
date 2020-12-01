package com.iamport.sdk.data.nice

// 뱅크페이 관련 (for PG:나이스 + 결제:실시간계좌이체)
object NiceBankpay {
    const val USER_KEY = "user_key" // 뱅크페이 intent 스킴
    const val CALLBACKPARAM = "callbackparam1" // 뱅크페이 intent 스킴
    const val CALLBACKPARAM2 = "callbackparam2"
    const val CODE = "bankpay_code" // 뱅크페이 결제완료 코드
    const val VALUE = "bankpay_value" // 뱅크페이 결제완료 밸류(포스트 전달 해야 함)
    const val INTENT_RESULT_NAME = "requestInfo"
}