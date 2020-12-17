package com.iamport.sdk.domain.utils

import com.orhanobut.logger.Logger

class PreventOverlapRun {

    private val delay = 550L // SDK open delay
    private var excuteTime: Long = 0 // SDK open time

    fun init() {
        excuteTime = 0
    }

    // 호출 강제 딜레이
    fun launch(delay: Long = this.delay, hof: () -> Unit) {
        if (System.currentTimeMillis() - excuteTime > delay) {
            excuteTime = System.currentTimeMillis()
            hof()
        } else {
            Logger.d("아직 딜레이야~")
        }
    }
}