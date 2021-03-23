package com.iamport.sdk.domain.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * https://thdev.tech/kotlin/2019/04/05/Init-Coroutines/
 */
interface BaseCoroutineScope : CoroutineScope {

    val job: Job

    /**
     * Coroutine job cancel
     */
    fun releaseCoroutine()
}