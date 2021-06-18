package com.iamport.sdk.data.chai.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PrepareData(
    val impUid: String,
    val paymentId: String?,
    val subscriptionId: String?,
    val idempotencyKey: String,
    val returnUrl: String,
    val publicAPIKey: String,
    val mode: String?,
    val isSbcr: Boolean?, // FIXME: 서버 배포 후 non nullable 로
) : Parcelable