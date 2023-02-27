package com.iamport.sdk.data.sdk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
https://docs.iamport.kr/tech/imp?lang=ko#param
 */
/**
 *
{
"error_msg": "F0005:결제가 중단되었습니다(imp_42234234).01 | 사용자가 결제를 취소 하였습니다.",
"imp_success": "false",
"imp_uid": "imp_42234234",
"merchant_uid": "mid_634534534548"
}
 */
// 모두 명세상 필수인지 모르겠음
// 이니시스 실시간 계좌이체 -> imp_success, success 없음?
@Parcelize
data class IamportResponse(
    val imp_success: Boolean? = false,
    val success: Boolean? = false,
    val imp_uid: String?,
    val merchant_uid: String?,
    val error_msg: String? = null,
    val error_code: String? = null,
) : Parcelable {
    companion object {
        fun makeSuccess(request: IamportRequest, impUid: String? = null, msg: String): IamportResponse {
            return makeSuccess(request.getMerchantUid(), impUid, msg)
        }

        fun makeFail(request: IamportRequest, impUid: String? = null, msg: String): IamportResponse {
            return makeFail(request.getMerchantUid(), impUid, msg)
        }

        fun makeSuccess(merchantUid: String, impUid: String? = null, msg: String): IamportResponse {
            return IamportResponse(
                imp_success = true,
                success = true,
                imp_uid = impUid,
                merchant_uid = merchantUid,
                error_msg = msg,
            )
        }

        fun makeFail(merchantUid: String, impUid: String? = null, msg: String): IamportResponse {
            return IamportResponse(
                imp_success = false,
                success = false,
                imp_uid = impUid,
                merchant_uid = merchantUid,
                error_msg = msg
            )
        }
    }
}
