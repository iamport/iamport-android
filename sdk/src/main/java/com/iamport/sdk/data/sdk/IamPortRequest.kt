package com.iamport.sdk.data.sdk

import android.os.Parcelable
import com.iamport.sdk.domain.utils.CONST
import kotlinx.android.parcel.Parcelize

/**
 * SDK 에 결제 요청할 데이터
 * https://docs.iamport.kr/tech/imp?lang=ko#param
 */
@Parcelize
data class IamPortRequest(
    val pg: String,
    val pay_method: PayMethod = PayMethod.card, // 명세상 필수인지 불명확함, default card
    val escrow: Boolean? = null, // default false
    val merchant_uid: String, // default "random"
    val name: String? = null,
    val amount: String,
    val custom_data: String? = null, // 명세상 불명확
    val tax_free: String? = null,
    val currency: Currency? = null, // default KRW, 페이팔은 USD 이어야 함
    val language: String? = null, // default "ko"
    val buyer_name: String? = null,
    val buyer_tel: String? = null,
    val buyer_email: String? = null,
    val buyer_addr: String? = null,
    val buyer_postcode: String? = null,
    val notice_url: List<String>? = null,
    val display: CardQuota? = null,
    val digital: Boolean? = null, // default false
    val vbank_due: String? = null, // YYYYMMDDhhmm
    val m_redirect_url: String? = CONST.IAMPORT_DUMMY_URL, // 콜백
    val app_scheme: String? = null, // 명세상 nullable 이나, RN 에서 필수
    val biz_num: String? = null,
    val popup: Boolean? = null // 명세상 없으나, RN 에 있음
) : Parcelable {

    /**
     * string pg 으로 enum PG 가져옴
     */
    val pgEnum: PG?
        get() {
            return PG.convertPG(pg)
        }
}


