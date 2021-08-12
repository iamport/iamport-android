package com.iamport.sdk.domain.utils

import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * https://thdev.tech/kotlin/2019/04/05/Init-Coroutines/
 */
class UICoroutineScope(private val dispatchers: CoroutineContext = Dispatchers.Main) : BaseCoroutineScope {

    override val job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = dispatchers + job

    override fun releaseCoroutine() {
        Logger.d("UICoroutineScope onRelease coroutine")
        job.cancel()
    }
}