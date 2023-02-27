package com.iamport.sdk.data.sdk

import android.os.Parcelable
import com.iamport.sdk.data.chai.response.ChaiPaymentStatus
import com.iamport.sdk.data.chai.response.PrepareData
import kotlinx.parcelize.Parcelize

@Parcelize
data class IamportApprove(
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
        fun make(request: IamportRequest, data: PrepareData, status: ChaiPaymentStatus): IamportApprove {
            return IamportApprove(
                userCode = request.userCode,
                merchantUid = request.getMerchantUid(),
                customerUid = request.getCustomerUid(),
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