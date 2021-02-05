package com.iamport.sdk.domain.utils

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.BroadcastReceiver
import android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.orhanobut.logger.Logger
import com.orhanobut.logger.Logger.d


object Foreground : ActivityLifecycleCallbacks {

    enum class AppStatus {
        BACKGROUND,  // app is background
        RETURNED_TO_FOREGROUND,  // app returned to foreground(or first launch)
        FOREGROUND, // app is foreground
    }

    var application: Application? = null
    var appStatus: AppStatus? = null

    val isBackground: Boolean // 백그라운드 여부
        get() = appStatus!!.ordinal == AppStatus.BACKGROUND.ordinal

    var isScreenOn: Boolean = true // 스크린 on/off 여부


    // running activity count
    private var running = 0


    fun init(app: Application) {
        application = app

        // 생명주기 콜백
        application?.registerActivityLifecycleCallbacks(this)
//        application?.unregisterActivityLifecycleCallbacks(this) // https://stackoverflow.com/questions/17865187/what-is-the-proper-way-to-unregister-activity-lifecycle-callbacks/23299321
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        isScreenOn = true
        if (++running == 1) {
            d("app is 포그라운드! 살아왔다")
            appStatus = AppStatus.RETURNED_TO_FOREGROUND
        } else if (running > 1) {
            d("app is 포그라운드")
            appStatus = AppStatus.FOREGROUND
        }
    }

    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {
        if (--running == 0) {
            d("app is 백그라운드")
            appStatus = AppStatus.BACKGROUND
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}


}