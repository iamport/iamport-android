package com.iamport.sdk.data.chai.response

import com.iamport.sdk.data.sdk.PG

data class UserData(
    val pg_provider: String?, // TODO: 2020-12-15 015 nullable 로 오는데.. 확인필요..
    val pg_id: String,
    val sandbox: Boolean?,
    val type: String,
)