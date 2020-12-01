package com.iamport.sdk.data.chai.response

import com.iamport.sdk.data.sdk.PG

data class UserData(
    val pg_provider: PG,
    val pg_id: String,
    val sandbox: Boolean,
    val type: String,
)