package com.iamport.sdk.domain.utils

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import com.orhanobut.logger.Logger
import java.lang.ref.WeakReference

enum class MODE {
    ACTIVITY, FRAGMENT, NONE
}

/**
 * SDK 실행 호스트 헬퍼 클래스
 */
class HostHelper(private val activityRef: WeakReference<ComponentActivity>? = null, private val fragmentRef: WeakReference<Fragment>? = null) {

//    lateinit var viewModelStoreOwner: WeakReference<ViewModelStoreOwner> // viewmodel 생성 위함
//    lateinit var lifecycleOwner: WeakReference<LifecycleOwner> // 뷰모델 라이브데이터 observe 위함
//    lateinit var lifecycle: WeakReference<Lifecycle> // 생명주기로 차이 앱 상태 체크위한 옵저버

    val mode: MODE = when {
        activityRef != null -> {
            MODE.ACTIVITY
        }
        fragmentRef != null -> {
            MODE.FRAGMENT
        }
        else -> {
            Logger.e("Err : Please input PureSDK parameters")
            MODE.NONE
        }
    }

    fun getActivityRef(): ComponentActivity? {
        return when (mode) {
            MODE.ACTIVITY -> {
                activityRef?.get()
            }
            MODE.FRAGMENT -> {
                fragmentRef?.get()?.activity
            }
            MODE.NONE -> {
                null
            }
        }
    }

    fun getFragmentRef(): Fragment? {
        return fragmentRef?.get()
    }

    fun getViewModelStoreOwner(): ViewModelStoreOwner? {
        return when (mode) {
            MODE.ACTIVITY -> {
                activityRef?.get()
            }
            MODE.FRAGMENT -> {
                fragmentRef?.get()
            }
            MODE.NONE -> {
                null
            }
        }
    }

    fun getLifecycleOwner(): LifecycleOwner? {
        return when (mode) {
            MODE.ACTIVITY -> {
                activityRef?.get()
            }
            MODE.FRAGMENT -> {
                fragmentRef?.get()?.viewLifecycleOwner
            }
            MODE.NONE -> {
                null
            }
        }
    }

    fun getLifecycle(): Lifecycle? {
        return when (mode) {
            MODE.ACTIVITY -> {
                activityRef?.get()?.lifecycle
            }
            MODE.FRAGMENT -> {
                fragmentRef?.get()?.lifecycle
            }
            MODE.NONE -> {
                null
            }
        }
    }

}