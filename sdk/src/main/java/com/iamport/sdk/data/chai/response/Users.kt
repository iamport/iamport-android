package com.iamport.sdk.data.chai.response

//* method : GET
//* 응답 content-type : application/json
//* URL : https://service.iamport.kr/users/pg/{아임포트 가맹점 식별코드}
//* 응답본문(Response)
data class Users(val code: Int, val msg: String, val data: ArrayList<UserData>)