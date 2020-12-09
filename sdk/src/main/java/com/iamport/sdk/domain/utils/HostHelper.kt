package com.iamport.sdk.domain.utils

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import com.orhanobut.logger.Logger

enum class MODE {
    ACTIVITY, FRAGMENT
}

/**
 * SDK 실행 호스트 헬퍼 클래스
 */
class HostHelper(var activity: ComponentActivity? = null, fragment: Fragment? = null) {
    lateinit var mode: MODE
    lateinit var viewModelStoreOwner: ViewModelStoreOwner
    lateinit var lifecycleOwner: LifecycleOwner
    lateinit var lifecycle: Lifecycle
    var context: Context? = null

    init {
        when {
            activity != null -> {
                activity?.let {
                    viewModelStoreOwner = it
                    lifecycleOwner = it
                    lifecycle = it.lifecycle
                    context = it.baseContext
                }
                mode = MODE.ACTIVITY
            }
            fragment != null -> {
                viewModelStoreOwner = fragment
                lifecycleOwner = fragment
                lifecycle = fragment.lifecycle
                activity = fragment.activity
                context = fragment.context
                mode = MODE.FRAGMENT
            }
            else -> {
                Logger.e("Err : Please input PureSDK parameters")
            }
        }
    }
}