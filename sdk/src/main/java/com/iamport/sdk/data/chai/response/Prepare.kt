package com.iamport.sdk.data.chai.response

//* Native SDK의 HTTP 요청 2 ( 결제시작 )
//* method : POST
//* content-type : application/json
//* URL : https://service.iamport.kr/chai_payments/prepare.json
//* 요청본문(Request)
/**
 * code 값이 0이면 CHAI로 "결제등록"이 정상처리되었음을 의미합니다.
 * code 값이 0이 아니면 비정상적인 상황이므로 msg 속성을 통해 에러메세지가 전달됩니다.
 * msg 속성은 아임포트에서 가공한 메세지이므로,
 * CHAI로부터 내려온 오류메세지 원본을 사용하시려면 data.errorMsg 속성을 참고하시면 됩니다.
 * 참고로, data.errorCode 속성은 reserved 필드로 의미없습니다.
 */

data class Prepare(val code: Int, val msg: String, val data: PrepareData)