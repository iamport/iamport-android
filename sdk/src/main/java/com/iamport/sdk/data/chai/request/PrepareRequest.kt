package com.iamport.sdk.data.chai.request

import com.iamport.sdk.data.chai.CHAI
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.PayMethod
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.Util
import com.orhanobut.logger.Logger

// * method : POST
//* content-type : application/json
//* URL : https://service.iamport.kr/chai_payments/prepare

data class PrepareRequest(
    val channel: String = CHAI.CHANNEL,//fixed
    val provider: PG = PG.chai, //fixed
    val pay_method: String = PayMethod.trans.name,//fixed
    val escrow: Boolean?, // true or false
    val amount: String, // 결제금액
    val tax_free: Float?, // 결제금액 중 면세공급가액,
    val name: String, //주문명,
    val merchant_uid: String, // 가맹점 주문번호,
    val customer_uid: String?, // 정기결제용
    val user_code: String, // 아임포트 가맹점 식별코드,
    val tier_code: String?, // 아임포트 agency 하위계정 tier code,
    val pg_id: String, // 차이계정 public Key, // 복수PG로직에 따라 Http 요청 1에서 받은 정보 + 요청인자 활용
    val buyer_name: String?, // 구매자 이름,
    val buyer_email: String?, // 구매자 Email,
    val buyer_tel: String?, // 구매자 전화번호,
    val buyer_addr: String?, // 구매자 주소,
    val buyer_postcode: String?, // 구매자 우편번호,
    val app_scheme: String?, // 결제 후 돌아갈 app scheme,
    val custom_data: String?, // 결제 건에 연결해 저장할 meta data,
    val notice_url: List<String>?, // Webhook Url,
    val confirm_url: String?, // Confirm process Url,
    val _extra: Extra // 차이 마케팅 팀과 사전협의된 파라메터
) {

    companion object {
        /**
         * 차이 앱에 요청하기 위한 리퀘스트 객체 생성
         */
        fun make(chaiId: String, payment: Payment): PrepareRequest? {
            val empty = CONST.EMPTY_STR
            return payment.iamPortRequest?.run {
                PrepareRequest(
                    escrow = false,
                    amount = amount,
                    tax_free = tax_free,
                    name = Util.getOrEmpty(name),
                    merchant_uid = merchant_uid,
                    customer_uid = customer_uid,
                    user_code = payment.userCode,
                    tier_code = empty,
                    pg_id = chaiId,
                    buyer_name = buyer_name,
                    buyer_email = buyer_email,
                    buyer_tel = buyer_tel,
                    buyer_addr = buyer_addr,
                    buyer_postcode = buyer_postcode,
                    app_scheme = app_scheme,
                    custom_data = custom_data,
                    notice_url = notice_url,
                    confirm_url = confirm_url,
                    _extra = Extra(native = OS.aos, bypass = empty)
                )
            } ?: run {
                Logger.d("PrepareRequest, make, iamPortRequest is null")
                null
            }
        }
    }
}