package com.iamport.sdk.data.sdk

import android.os.Parcelable
import com.iamport.sdk.data.chai.response.PrepareData
import kotlinx.parcelize.Parcelize

@Parcelize
data class IamPortApprove(
    val userCode: String,
    val merchantUid: String,
    val paymentId: String?,
    val impUid: String?,
    val idempotencyKey: String?,
    val publicAPIKey: String?,
    val msg: String? = null
) : Parcelable {

    companion object {
        fun make(payment: Payment, data: PrepareData): IamPortApprove {
            return IamPortApprove(
                userCode = payment.userCode,
                merchantUid = payment.getMerchantUid(),
                paymentId = data.paymentId,
                impUid = data.impUid,
                idempotencyKey = data.idempotencyKey,
                publicAPIKey = data.publicAPIKey
            )
        }
    }
}