package com.iamport.sdk.domain.strategy.base

import com.google.gson.Gson
import com.iamport.sdk.data.sdk.IamportResponse
import com.iamport.sdk.data.sdk.IamportRequest
import com.iamport.sdk.domain.di.IamportKoinComponent
import com.iamport.sdk.domain.utils.Constant
import com.iamport.sdk.domain.utils.Event
import com.iamport.sdk.domain.utils.NativeLiveDataEventBus
import kotlinx.coroutines.CancellationException
import org.koin.core.component.inject
import org.koin.core.qualifier.named

abstract class BaseStrategy : IStrategy, IamportKoinComponent {

    protected val gson: Gson by inject(named("${Constant.KOIN_KEY}Gson"))
    protected val bus: NativeLiveDataEventBus by inject()
    lateinit var request: IamportRequest

    override fun init() {}

    override suspend fun doWork(request: IamportRequest) {
        super.doWork(request)
        this.request = request
    }

    /**
     * SDK 종료
     */
    override fun sdkFinish(response: IamportResponse?) {
        bus.impResponse.value = Event(response)
    }

    infix fun <T> Result<T>.catchNotCancelled(block: (Throwable) -> Unit) =
        exceptionOrNull()?.let {
            if (it !is CancellationException)
                block(it)
        }
}