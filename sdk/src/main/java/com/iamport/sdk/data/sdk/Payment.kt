package com.iamport.sdk.data.sdk

import android.os.Parcelable
import com.iamport.sdk.domain.utils.CONST
import kotlinx.parcelize.Parcelize

// 가상계좌 -> vbank_due 입력
// 다날의 가상계좌 결제 -> 사업자 등록번호 필수입력 biz_num
// 그냥 휴대폰 소액결제 -> digital 필수입력
// 페이팔 -> m_redirect_url 필수
@Parcelize
data class Payment(val userCode: String, val iamPortRequest: IamPortRequest) : Parcelable {

    companion object {
        fun validator(payment: Payment): Pair<Boolean, String?> {

            payment.iamPortRequest.run {
                if (pay_method == PayMethod.vbank) {
                    if (vbank_due.isNullOrBlank()) {
                        return false to "가상계좌 결제는 만료일자(vbank_due) 항목 필수입니다 (YYYYMMDDhhmm 형식)"
                    }
                }

                if (pay_method == PayMethod.phone) {
                    if (digital == null) {
                        return false to "휴대폰 소액결제는 digital 항목 필수입니다"
                    }
                }

                if (PG.convertPG(pg) == PG.danal_tpay && pay_method == PayMethod.vbank) {
                    if (biz_num.isNullOrBlank()) {
                        return false to "다날 가상계좌 결제는 사업자 등록번호(biz_num) 항목 필수입니다 (계약된 사업자등록번호 10자리)"
                    }
                }

                if (PG.convertPG(pg) == PG.paypal) {
                    if (m_redirect_url.isNullOrBlank() || m_redirect_url == CONST.IAMPORT_DUMMY_URL) {
                        return false to "페이팔 결제는 m_redirect_url 항목 필수입니다"
                    }
                }
            }

            return true to null
        }
    }
}