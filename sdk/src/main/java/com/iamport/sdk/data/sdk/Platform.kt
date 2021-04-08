package com.iamport.sdk.data.sdk

import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.Util

enum class Platform(val redirectUrl: String) {
    native(CONST.IAMPORT_DETECT_URL),
    reactnative(Util.getRedirectUrl("rn")),
    flutter(Util.getRedirectUrl("flu")),
    cordova(Util.getRedirectUrl("cor")),
    capacitor(Util.getRedirectUrl("cap"));


    companion object {
        fun convertPlatform(platformStr: String): Platform? {
            return values().find { platformStr == (it.name) }
        }
    }
}