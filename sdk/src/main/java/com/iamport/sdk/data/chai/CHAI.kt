package com.iamport.sdk.data.chai

import com.iamport.sdk.domain.utils.CONST
import com.orhanobut.logger.Logger

// 차이 네이티브 결제 관련
object CHAI {
//    var pkg: String? = null
    const val SINGLE_ACTIVITY_VERSION: Long = 2000169
    const val PAYMENT_ID = "paymentId"
    const val SUBSCRIPTION_ID = "subscriptionId"
    const val IDEMPOTENCY_KEY = "idempotencyKey"
    const val STATUS = "status"
    const val NATIVE = "native"
    const val CHANNEL = "mobile"
}

// 차이 mode 설정에 따라 chai server url 이 다름
enum class CHAI_MODE(val url: String) {
    prod(CONST.CHAI_SERVICE_URL),
    staging(CONST.CHAI_SERVICE_STAGING_URL),
    dev(CONST.CHAI_SERVICE_DEV_URL);

    companion object {
        fun getChaiUrl(mode: String?): String {
            return values().find { mode == (it.name) }?.url ?: run {
                Logger.w("Not found CHAI mode => [$mode]")
                prod.url // default
            }
        }
    }
}