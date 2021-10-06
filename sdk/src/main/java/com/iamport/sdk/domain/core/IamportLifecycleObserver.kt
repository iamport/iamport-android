package com.iamport.sdk.domain.core

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.iamport.sdk.presentation.contract.BankPayContract
import com.orhanobut.logger.Logger

class IamportLifecycleObserver(private val registry: ActivityResultRegistry) : DefaultLifecycleObserver {
    private lateinit var bankPayLauncher: ActivityResultLauncher<String>
    lateinit var resultCallback: (Pair<String, String>) -> Unit

    override fun onCreate(owner: LifecycleOwner) {
        bankPayLauncher = registry.register("key", owner, BankPayContract()) { res: Pair<String, String>? ->
            res?.let {
//                viewModel.processBankPayPayment(res)
                resultCallback.invoke(it)
            } ?: Logger.e("NICE TRANS result is NULL")
        }
    }

    fun bankPayLaunch(it: String, resultCallback : (Pair<String, String>) -> Unit) {
        this.resultCallback = resultCallback
        bankPayLauncher.launch(it)
    }
}
