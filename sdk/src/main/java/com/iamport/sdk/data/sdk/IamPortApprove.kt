package com.iamport.sdk.data.sdk

import android.os.Parcelable
import com.iamport.sdk.data.chai.response.ChaiPaymentStatus
import com.iamport.sdk.data.chai.response.PrepareData
import kotlinx.parcelize.Parcelize

@Parcelize
data class IamPortApprove(
    val userCode: String,
    val merchantUid: String,
    val customerUid: String?,
    val paymentId: String?,
    val subscriptionId: String?,
    val impUid: String,
    val idempotencyKey: String,
    val publicAPIKey: String,
    var status: ChaiPaymentStatus,
    val msg: String? = null
) : Parcelable {

    companion object {
        fun make(payment: Payment, data: PrepareData, status: ChaiPaymentStatus): IamPortApprove {
            return IamPortApprove(
                userCode = payment.userCode,
                merchantUid = payment.getMerchantUid(),
                customerUid = payment.getCustomerUid(),
                paymentId = data.paymentId,
                subscriptionId = data.subscriptionId,
                impUid = data.impUid,
                idempotencyKey = data.idempotencyKey,
                publicAPIKey = data.publicAPIKey,
                status = status
            )
        }
    }
}