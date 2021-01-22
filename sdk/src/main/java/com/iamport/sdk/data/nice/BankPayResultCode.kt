package com.iamport.sdk.data.nice

enum class BankPayResultCode(val code: String) {
    OK("000"), CANCEL("091"),
    TIME_OUT("060"), FAIL_SIGN("050"),
    FAIL_OTP("040"), FAIL_CERT_MODULE_INIT("030");

    companion object {
        fun from(s: String): BankPayResultCode? = values().find { it.code == s }
        fun desc(code: BankPayResultCode): String {
            return when (code) {
                OK -> "결제성공 하였습니다"
                CANCEL -> "계좌이체 결제를 취소하였습니다."
                TIME_OUT -> "타임아웃"
                FAIL_SIGN -> "전자서명 실패"
                FAIL_OTP -> "OTP/보안카드 처리 실패"
                FAIL_CERT_MODULE_INIT -> "인증모듈 초기화 오류"
            }
        }
    }
}