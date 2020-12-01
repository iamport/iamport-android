package com.iamport.sdk.domain.utils

import com.orhanobut.logger.Logger

class DelayRun {

    private val delay = 550L // SDK open delay
    private var excuteTime: Long = 0 // SDK open time

    // 호출 강제 딜레이
    fun launch(delay: Long = this.delay, hof: () -> Unit) {
        if (System.currentTimeMillis() - excuteTime > delay) {
            excuteTime = System.currentTimeMillis()
            hof()
        } else {
            Logger.i("아직 딜레이야~")
        }
    }
}