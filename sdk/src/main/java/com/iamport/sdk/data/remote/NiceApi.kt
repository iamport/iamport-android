package com.iamport.sdk.data.remote

import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

// 일단 만들어놨는데, 나이스 PG + 실시간 최종 결제인데, 요청 후 응답 값을 특정할 수가 없음
// 그래서 레거시 처럼 webview 에 posturl 로 요청하고 내려오는 m_redirect_url 사용
interface NiceApi {

    @FormUrlEncoded
    @POST
    suspend fun payments(@Url url: String, @FieldMap post: Map<String, String>)
}