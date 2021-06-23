package com.iamport.sdk.data.chai.response

data class ChaiPaymentSubscription(
    val subscriptionId: String,
    val status: String,
    val displayStatus: String,
    val idempotencyKey: String,
    val checkoutAmount: Float,
    val returnUrl: String,
    val description: String,
    val merchantUserId: String,
    val createdAt: String, // "2020-10-27T06:36:12.218Z",
    val updatedAt: String, // "2020-10-27T06:36:12.218Z",
): BaseChaiPayment()