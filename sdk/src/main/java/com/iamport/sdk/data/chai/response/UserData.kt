package com.iamport.sdk.data.chai.response

import com.iamport.sdk.data.sdk.PG

// PG정보가 없어도(회원가입 후 즉시 or PG 를 API 방식으로만 이용하는 경우 or 본인인증만 이용하는 경우) 에도
// pg_id 가 null 로 해당 데이터가 하나는 무조건 있으므로 다 nullable 처리한다 ㅠ
data class UserData(
    val pg_provider: String?,
    val pg_id: String?,
    val sandbox: Boolean?,
    val type: String?,
)