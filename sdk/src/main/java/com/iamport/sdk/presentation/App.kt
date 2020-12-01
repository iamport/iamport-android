package com.iamport.sdk.presentation

import android.app.Application
import com.iamport.sdk.BuildConfig.DEBUG
import com.iamport.sdk.domain.di.appModule
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.Foreground
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import org.koin.android.ext.koin.androidContext
import org.koin.android.logger.AndroidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Foreground.init(this)

        val formatStrategy: PrettyFormatStrategy = if (DEBUG) {
            PrettyFormatStrategy.newBuilder()
                .methodCount(3)
                .tag(CONST.IAMPORT_LOG)
                .build()
        } else {
            PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)         // (Optional) How many method line to show. Default 2
                .methodOffset(5)        // (Optional) Hides internal method calls up to offset. Default 5
                .tag(CONST.IAMPORT_LOG)
                .build()
        }
        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))

        startKoin {
            logger(AndroidLogger(Level.DEBUG))
            androidContext(this@App)
            modules(appModule)
        }

    }
}