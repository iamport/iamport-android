package com.iamport.sampleapp

import android.app.Application
import com.iamport.sdk.domain.core.Iamport


class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        /**
         * activity 를 통해 application 을 가져올 수 있기에 제거
         */
//        Iamport.create(this)

        /**
         * DI 로 KOIN 사용시 아래와 같이 사용
        val koinApp = startKoin {
            logger(AndroidLogger())
            androidContext(this@BaseApplication)
        }
        Iamport.createWithKoin(this, koinApp)
         */
    }
}

