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
                        return false to CONST.ERR_PAYMENT_VALIDATOR_VBANK
                    }
                }

                if (pay_method == PayMethod.phone) {
                    if (digital == null) {
                        return false to CONST.ERR_PAYMENT_VALIDATOR_PHONE
                    }
                }

                if (PG.convertPG(pg) == PG.danal_tpay && pay_method == PayMethod.vbank) {
                    if (biz_num.isNullOrBlank()) {
                        return false to CONST.ERR_PAYMENT_VALIDATOR_DANAL_VBANK
                    }
                }

//                if (PG.convertPG(pg) == PG.paypal) {
//                    if (m_redirect_url.isNullOrBlank() || m_redirect_url == CONST.IAMPORT_DETECT_URL) {
//                        return false to CONST.ERR_PAYMENT_VALIDATOR_PAYPAL
//                    }
//                }
            }

            return true to null
        }
    }
}