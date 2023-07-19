package com.iamport.sampleapp

import android.app.Application

/**
 * 해당 Custom Application class 를 더이상 사용하지 않습니다 (sdk version > 1.2.0)
 */
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        /**
         * 아래 create 관련 함수를 더이상 사용하지 않습니다.
         * Iamport.create(this)
         * Iamport.createWithKoin(this, koinApp)
         */
    }
}

