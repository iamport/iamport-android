package com.iamport.sdk.domain.utils

object CONST {
    const val KOIN_KEY = "iamport-koin-key"
    const val APP_SCHME = "iamport_android"
    const val HTTP_SCHEME = "http"
    const val HTTPS_SCHEME = "https"
    const val ABOUT_BLANK_SCHEME = "about"

    const val EMPTY_STR = ""

    const val IMP_USER_CODE = "impUserCode"
    const val IMP_UID = "impUid"
    const val IMP_CUSTOMER_UID = "impCustomerUid"
    const val PAYMENT_WEBVIEW_JS_INTERFACE_NAME = "IAMPORT"

    const val NICE_PG_PROVIDER = "nice"

    // 이 url 로 감지되면, 결제완료 콜백이란 의미 + 붙은 파라미터로 결제결과 처리
    const val IAMPORT_DETECT_SCHEME = "$HTTP_SCHEME://"
    const val IAMPORT_DETECT_ADDRESS = "detectchangingwebview/iamport/a" // a == android
    const val IAMPORT_DETECT_URL = "$IAMPORT_DETECT_SCHEME$IAMPORT_DETECT_ADDRESS"

    const val IAMPORT_PROD_URL = "https://service.iamport.kr"
    const val IAMPORT_TEST_URL = "http://1b8309246be2.ngrok.io"
//    const val IAMPORT_TEST_URL = "https://kicc.iamport.kr"


    const val CHAI_SERVICE_URL = "https://api.chai.finance"
    const val CHAI_SERVICE_DEV_URL = "https://api-dev.chai.finance"
    const val CHAI_SERVICE_STAGING_URL = "https://api-staging.chai.finance"

    const val PAYMENT_PLAY_STORE_URL = "market://details?id="

    const val PAYMENT_FILE_URL = "file:///android_asset/iamportcdn.html"
    const val PAYMENT_MOBILE_WEB_FILE_URL = "file:///android_asset/mobileweb.html"

    const val IAMPORT_LOG = "IAMPORT"

    const val CONTRACT_INPUT = "input"
    const val CONTRACT_OUTPUT = "output"
    const val BUNDLE_PAYMENT = "payment"


    const val POLLING_DELAY = 1000L
    const val TIME_OUT_MIN = 5 // 분

    const val TIME_OUT = TIME_OUT_MIN * 60000L // 차이 폴링 타임아웃

    private const val TRY_OUT_ONE_MIN = 60000L / POLLING_DELAY // 1분 단위
    const val TRY_OUT_COUNT = TRY_OUT_ONE_MIN * TIME_OUT_MIN // 차이 폴링 타임아웃

    const val CHAI_FINAL_PAYMENT_TIME_OUT_SEC = 6 * POLLING_DELAY // 차이 최종결제 위한 머천트 컨펌 타임아웃

    const val BROADCAST_FOREGROUND_SERVICE = "com.iamport.sdk.broadcast.fgservice"
    const val BROADCAST_FOREGROUND_SERVICE_STOP = "com.iamport.sdk.broadcast.fgservice.stop"


    const val USER_TYPE_PAYMENT = "payment"
    const val USER_TYPE_CERTIFICATION = "certification"


    // payment 객체 validation 관련
    const val PASS_PAYMENT_VALIDATOR = "성공"

    private const val PREFIX_ERR = "[SDK ERR]"
    const val ERR_PAYMENT_VALIDATOR_VBANK = "$PREFIX_ERR 가상계좌 결제는 만료일자(vbank_due) 항목 필수입니다 (YYYYMMDDhhmm 형식)"
    const val ERR_PAYMENT_VALIDATOR_PHONE = "$PREFIX_ERR 휴대폰 소액결제는 digital 항목 필수입니다"
    const val ERR_PAYMENT_VALIDATOR_DANAL_VBANK = "$PREFIX_ERR 다날 가상계좌 결제는 사업자 등록번호(biz_num) 항목 필수입니다 (계약된 사업자등록번호 10자리)"
    const val ERR_PAYMENT_VALIDATOR_EXIMBAY = "$PREFIX_ERR eximbay 는 모바일앱 결제시 IamPortRequest popup 파라미터를 false 로 지정해야 결제창이 열립니다."
//    const val ERR_PAYMENT_VALIDATOR_PAYPAL = "$PREFIX_ERR 페이팔 결제는 m_redirect_url 항목 필수입니다"

}