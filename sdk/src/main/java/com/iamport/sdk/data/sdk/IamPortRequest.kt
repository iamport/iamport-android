package com.iamport.sdk.data.sdk

import android.os.Parcelable
import com.iamport.sdk.domain.utils.Constant
import com.orhanobut.logger.Logger
import kotlinx.parcelize.Parcelize

// 가상계좌 -> vbank_due 입력
// 다날의 가상계좌 결제 -> 사업자 등록번호 필수입력 biz_num
// 그냥 휴대폰 소액결제 -> digital 필수입력
// 페이팔 -> m_redirect_url 필수
@Parcelize
data class IamportRequest(
    val userCode: String,
    val tierCode: String? = null,
    val iamportPayment: IamportPayment? = null,
    val iamPortCertification: IamportCertification? = null
) : Parcelable {

    enum class STATUS {
        PAYMENT, CERT, ERROR
    }

    fun getStatus(): STATUS {

        if (iamPortCertification == null && iamportPayment == null) {
            Logger.e("ERR : iamPortCertification & iamPortRequest NULL")
            return STATUS.ERROR
        }

        return iamPortCertification?.run {
            STATUS.CERT
        } ?: run {
            STATUS.PAYMENT
        }
    }

    fun getMerchantUid(): String {
        return when(getStatus()) {
            STATUS.PAYMENT -> iamportPayment?.merchant_uid ?: Constant.EMPTY_STR
            STATUS.CERT -> iamPortCertification?.merchant_uid ?: Constant.EMPTY_STR
            STATUS.ERROR -> Constant.EMPTY_STR
        }
    }

    fun getCustomerUid(): String {
        return when(getStatus()) {
            STATUS.PAYMENT -> iamportPayment?.customer_uid ?: Constant.EMPTY_STR
            STATUS.CERT -> Constant.EMPTY_STR
            STATUS.ERROR -> Constant.EMPTY_STR
        }
    }

    companion object {
        fun validator(IamportRequest: IamportRequest): Pair<Boolean, String?> {

            if (IamportRequest.iamPortCertification == null && IamportRequest.iamportPayment == null) {
                Logger.e("ERR : iamPortCertification & iamPortRequest NULL")
                return false to "ERR : iamPortCertification & iamPortRequest NULL"
            }

            IamportRequest.iamportPayment?.run {
                if (pay_method == PayMethod.vbank.name) {
                    if (vbank_due.isNullOrBlank()) {
                        return false to Constant.ERR_PAYMENT_VALIDATOR_VBANK
                    }
                }

                if (pay_method == PayMethod.phone.name) {
                    if (digital == null) {
                        return false to Constant.ERR_PAYMENT_VALIDATOR_PHONE
                    }
                }

                if (PG.convertPG(pg) == PG.danal_tpay && pay_method == PayMethod.vbank.name) {
                    if (biz_num.isNullOrBlank()) {
                        return false to Constant.ERR_PAYMENT_VALIDATOR_DANAL_VBANK
                    }
                }

                // 엑심베이eximbay 의 경우 popup 파라미터를 false 로 해야 redirect 로 열림
                if (PG.convertPG(pg) == PG.eximbay) {
                    if (popup == null || popup == true) {
                        return false to Constant.ERR_PAYMENT_VALIDATOR_EXIMBAY
                    }
                }

//                if (PG.convertPG(pg) == PG.paypal) {
//                    if (m_redirect_url.isNullOrBlank() || m_redirect_url == Constant.IAMPORT_DETECT_URL) {
//                        return false to Constant.ERR_PAYMENT_VALIDATOR_PAYPAL
//                    }
//                }
            }

            return true to Constant.PASS_PAYMENT_VALIDATOR
        }
    }
}