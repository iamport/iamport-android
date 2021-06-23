package com.iamport.sdk.data.chai.response

enum class ChaiPaymentStatus {
    waiting, prepared,
    approved,
    user_canceled, canceled, failed, timeout,
    confirmed, partial_confirmed, inactive, churn;

    companion object {
        fun from(displayStatus: String): ChaiPaymentStatus? = values().find { it.name == displayStatus }
    }
}