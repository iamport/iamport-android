package com.iamport.sdk.data.chai.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PrepareData(
    val impUid: String,
    val paymentId: String?,
    val idempotencyKey: String?,
    val returnUrl: String?,
    val publicAPIKey: String?,
    val mode: String?,
) : Parcelable