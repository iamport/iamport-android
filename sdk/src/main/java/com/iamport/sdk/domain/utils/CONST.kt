package com.iamport.sdk.domain.utils

object CONST {
    const val APP_SCHME = "iamport_android"
    const val HTTP_SCHEME = "http"
    const val HTTPS_SCHEME = "https"
    const val ABOUT_BLANK_SCHEME = "about:blank"

    const val EMPTY_STR = ""

    const val IMP_USER_CODE = "impUserCode"
    const val IMP_UID = "impUid"
    const val PAYMENT_WEBVIEW_JS_INTERFACE_NAME = "IAMPORT"

    const val NICE_PG_PROVIDER = "nice"

    // 이 url 로 감지되면, 결제완료 콜백이란 의미 + 붙은 파라미터로 결제결과 처리
    const val IAMPORT_DUMMY_URL = "http://localhost/iamport"

    const val IAMPORT_PROD_URL = "https://service.iamport.kr" // 테스트도 상용서버에서
//    const val IAMPORT_TEST_URL = "https://kicc.iamport.kr"


    const val CHAI_SERVICE_URL = "https://api.chai.finance"
    const val CHAI_SERVICE_STAGING_URL = "https://api-staging.chai.finance"

    const val PAYMENT_PLAY_STORE_URL = "market://details?id="

    const val PAYMENT_FILE_URL = "file:///android_asset/iamportcdn.html"

    const val IAMPORT_LOG = "IAMPORT"

    const val CONTRACT_INPUT = "input"
    const val CONTRACT_OUTPUT = "output"
    const val BUNDLE_PAYMENT = "payment"


    const val POLLING_DELAY = 1000L
    private const val TRY_OUT_ONE_MIN = 60000L / POLLING_DELAY // 1분 단위
    const val TRY_OUT_MIN = 5 // 분
    const val TRY_OUT_COUNT = TRY_OUT_ONE_MIN * TRY_OUT_MIN // 차이 폴링 타임아웃
//    const val TRY_OUT_COUNT = 15

    const val CHAI_FINAL_PAYMENT_TIME_OUT_SEC = 6 * POLLING_DELAY // 차이 최종결제 위한 머천트 컨펌 타임아웃

    const val BROADCAST_FOREGROUND_SERVICE = "com.iamport.sdk.broadcast.fgservice"

}