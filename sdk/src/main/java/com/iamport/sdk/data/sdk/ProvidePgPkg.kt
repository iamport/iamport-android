package com.iamport.sdk.data.sdk


// url 에서 제공하지 않는 PG와 앱 패키지
enum class ProvidePgPkg(val scheme: String, val pkg: String) {
    BANKPAY("kftc-bankpay", "com.kftc.bankpay.android"),
    ISP("ispmobile", "kvp.jjy.MispAndroid320"),
    KB_BANKPAY("kb-bankpay", "com.kbstar.liivbank"),
    NH_BANKPAY("nhb-bankpay", "com.nh.cashcardapp"),
    MG_BANKPAY("mg-bankpay", "kr.co.kfcc.mobilebank"),
    KN_BANKPAY("kn-bankpay", "com.knb.psb"),
    TOSS_TRANS_PAYNOW("lguthepay-xpay", "com.lguplus.paynow");

    companion object {
        fun from(s: String): ProvidePgPkg? = values().find { it.scheme == s }

        fun getNiceBankPayPrefix(): String {
            val process = "://eftpay?"
            return "${BANKPAY.scheme}${process}"
        }

        fun getNiceBankPayAppCls(): String {
            return "${BANKPAY.pkg}.activity.MainActivity"
        }
    }

}