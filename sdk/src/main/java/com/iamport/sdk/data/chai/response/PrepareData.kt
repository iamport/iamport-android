package com.iamport.sdk.data.chai.response

data class PrepareData(
    val impUid: String,
    val paymentId: String?,
    val idempotencyKey: String?,
    val returnUrl: String?,
    val publicAPIKey: String?,
    val mode: String?,
)