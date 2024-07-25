package com.iamport.sdk.data.sdk

import com.iamport.sdk.domain.utils.CONST

//html5_inicis(이니시스웹표준)
//inicis(이니시스ActiveX결제창)
//kcp(NHN KCP)
//kcp_billing(NHN KCP 정기결제)
//uplus(LGU+)
//nice(나이스페이)
//jtnet(JTNet)
//kicc(한국정보통신)
//bluewalnut(블루월넛)
//kakaopay(카카오페이)
//danal(다날휴대폰소액결제)
//danal_tpay(다날일반결제)
//mobilians(모빌리언스 휴대폰소액결제)
//chai(차이 간편결제)
//syrup(시럽페이)
//payco(페이코)
//paypal(페이팔)
//eximbay(엑심베이)
//naverpay(네이버페이-결제형)
//naverco(네이버페이-주문형)
//smilepay(스마일페이)
//alipay(알리페이)

/** https://docs.iamport.kr/tech/imp?lang=ko#param */
enum class PG(val korName: String) {
    chai("차이 간편결제"), kcp("NHN KCP"),
    html5_inicis("이니시스웹표준"), /* inicis("이니시스본인인증"), */ kcp_billing("NHN KCP 정기결제"),
    uplus("토스페이먼츠 - (구)LG유플러스"), jtnet("JTNet"), kakaopay("카카오페이"), nice("나이스페이"),
    /*kakao("카카오"), */ danal("다날휴대폰소액결제"), danal_tpay("다날일반결제"),
    kicc("한국정보통신"), paypal("페이팔"), mobilians("모빌리언스 휴대폰소액결제"),
    payco("페이코"), eximbay("엑심베이"), settle("세틀뱅크"), settle_firm("세틀뱅크_펌"),
    /*naverco("네이버페이-주문형"),*/ naverpay("네이버페이-결제형"), smilepay("스마일페이"),
    payple("페이플"), alipay("알리페이"),
    bluewalnut("bluewalnut"), tosspay("간편결제 - 토스"), smartro("스마트로"), nice_v2("나이스페이 V2"); /*, inicis("이니시스ActiveX결제창"), syrup("시럽페이");*/

    fun makePgRawName(pgId: String? = null): String {
        return "${this.name}${if (!pgId.isNullOrBlank()) ".${pgId}" else CONST.EMPTY_STR}"
    }

    companion object {
        fun convertPG(pgString: String): PG? {
            return entries.find { pgString == (it.name) }
        }

        fun getPGNames(): List<String> {
            return entries.map { "${it.korName} (${it.name})" }.toList()
        }
    }
}