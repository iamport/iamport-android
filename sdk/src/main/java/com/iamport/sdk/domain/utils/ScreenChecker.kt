package com.iamport.sdk.domain.utils

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.orhanobut.logger.Logger.d
import java.lang.ref.WeakReference


object ScreenChecker : ActivityLifecycleCallbacks {

    enum class AppStatus {
        BACKGROUND,  // app is background
        RETURNED_TO_FOREGROUND,  // app returned to foreground(or first launch)
        FOREGROUND, // app is foreground
    }

    // running activity count
    private var running = 0
    private var appStatus: AppStatus? = null

    // check data
    val isBackground: Boolean // 백그라운드 여부
        get() = appStatus?.ordinal == AppStatus.BACKGROUND.ordinal

    var isScreenOn: Boolean = true // 스크린 on/off 여부

//    private var activityWeakRef: WeakReference<Activity>? = null
    private var applicationWeakRef: WeakReference<Application>? = null

    @JvmStatic
    fun init(app: Application) {
        // 생명주기 콜백
        applicationWeakRef = WeakReference(app)
//        app.unregisterActivityLifecycleCallbacks(this) // 고민 : https://stackoverflow.com/questions/17865187/what-is-the-proper-way-to-unregister-activity-lifecycle-callbacks/23299321
        app.registerActivityLifecycleCallbacks(this)
    }

//    private fun setTopActivityWeakRef(activity: Activity) {
//        Logger.i("activityWeakRef ${activity.packageName}")
//        if (activityWeakRef == null || activity != (activityWeakRef as WeakReference<Activity>).get()) {
//            activityWeakRef = WeakReference(activity)
//        }
//    }
//
//    @JvmStatic
//    fun getActivtyReference(): Activity? {
//        if (activityWeakRef != null) {
//            val activity = (activityWeakRef as WeakReference<Activity>).get()
//            if (activity != null) {
//                return activity
//            }
//        }
//
//        return null
//    }


    // MainViewModel 에서 참조할 수 있음
//    @JvmStatic
//    fun getContext(): Context {
//        return getActivtyReference() ?: mApplicationWeakRef?.get() as Context
//    }


    // 포그라운드
    override fun onActivityStarted(activity: Activity) {
//        setTopActivityWeakRef(activity)
        isScreenOn = true
        if (++running == 1) {
            d("app is 포그라운드! 살아왔다")
            appStatus = AppStatus.RETURNED_TO_FOREGROUND
        } else if (running > 1) {
            d("app is 포그라운드")
            appStatus = AppStatus.FOREGROUND
        }
    }

    // 백그라운드
    override fun onActivityStopped(activity: Activity) {
//        setTopActivityWeakRef(activity)
        if (--running == 0) {
            d("app is 백그라운드")
            appStatus = AppStatus.BACKGROUND
        }
    }


    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
//        setTopActivityWeakRef(activity)
    }

    override fun onActivityResumed(activity: Activity) {
//        setTopActivityWeakRef(activity)
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}


}