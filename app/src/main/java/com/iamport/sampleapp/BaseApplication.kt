package com.iamport.sampleapp

import android.app.Application
import com.iamport.sdk.domain.core.Iamport
import org.koin.android.ext.koin.androidContext
import org.koin.android.logger.AndroidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Iamport.create(this)

        /**
         * DI 로 KOIN 사용시 아래와 같이 사용
        val koinApp = startKoin {
            logger(AndroidLogger())
            androidContext(this@BaseApplication)
        }
        Iamport.create(this, koinApp)
         */
    }
}