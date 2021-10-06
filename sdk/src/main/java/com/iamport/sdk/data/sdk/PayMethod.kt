package com.iamport.sdk.data.sdk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//card(신용카드)
//trans(실시간계좌이체)
//vbank(가상계좌)
//phone(휴대폰소액결제)
//samsung(삼성페이 / 이니시스, KCP 전용)
//kpay(KPay앱 직접호출 / 이니시스 전용)
//kakaopay(카카오페이 직접호출 / 이니시스, KCP, 나이스페이먼츠 전용)
//payco(페이코 직접호출 / 이니시스, KCP 전용)
//lpay(LPAY 직접호출 / 이니시스 전용)
//ssgpay(SSG페이 직접호출 / 이니시스 전용)
//tosspay(토스간편결제 직접호출 / 이니시스 전용)
//cultureland(문화상품권 / 이니시스, LGU+, KCP 전용)
//smartculture(스마트문상 / 이니시스, LGU+, KCP 전용)
//happymoney(해피머니 / 이니시스, KCP 전용)
//booknlife(도서문화상품권 / LGU+, KCP 전용)
//point(베네피아 포인트 등 포인트 결제 / KCP 전용)

/** https://docs.iamport.kr/tech/imp?lang=ko#param */
@Parcelize
enum class PayMethod(val korName: String) : Parcelable {
    card("신용카드"), trans("실시간계좌이체"), vbank("가상계좌"), phone("휴대폰소액결제"),
    samsung("삼성페이 / 이니시스, KCP 전용"), kpay("KPay앱 직접호출 / 이니시스 전용"),
    kakaopay("카카오페이 직접호출 / 이니시스, KCP, 나이스페이먼츠 전용"), payco("페이코 직접호출 / 이니시스, KCP 전용"),
    lpay("LPAY 직접호출 / 이니시스 전용"), ssgpay("SSG페이 직접호출 / 이니시스 전용"),
    tosspay("토스간편결제 직접호출 / 이니시스 전용"), cultureland("문화상품권 / 이니시스, LGU+, KCP 전용"),
    smartculture("스마트문상 / 이니시스, LGU+, KCP 전용"), happymoney("해피머니 / 이니시스, KCP 전용"),
    booknlife("도서문화상품권 / LGU+, KCP 전용"), point("베네피아 포인트 등 포인트 결제 / KCP 전용"),
    unionpay("유니온페이"), alipay("알리페이"), tenpay("텐페이"), wechat("위챗페이"),
    molpay("몰페이"), paysbuy("태국 paysbuy"), naverpay("네이버페이");

    fun getPayMethodName(): String {
        return "$korName ($name)"
    }

    companion object {
        fun from(payMethodString: String): PayMethod {
            return values().find { payMethodString == (it.name) } ?: card
        }
    }

}