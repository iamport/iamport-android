package com.iamport.sdk.domain.strategy.base

import com.google.gson.Gson
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.di.IamportKoinComponent
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.Event
import com.iamport.sdk.domain.utils.NativeLiveDataEventBus
import kotlinx.coroutines.CancellationException
import org.koin.core.component.inject
import org.koin.core.qualifier.named

abstract class BaseStrategy : IStrategy, IamportKoinComponent {

    protected val gson: Gson by inject(named("${CONST.KOIN_KEY}Gson"))
    protected val bus: NativeLiveDataEventBus by inject()
    lateinit var payment: Payment

    override fun init() {}

    override suspend fun doWork(payment: Payment) {
        super.doWork(payment)
        this.payment = payment
    }

    /**
     * SDK 종료
     */
    override fun sdkFinish(response: IamPortResponse?) {
        bus.impResponse.value = Event(response)
    }

    infix fun <T> Result<T>.catchNotCancelled(block: (Throwable) -> Unit) =
        exceptionOrNull()?.let {
            if (it !is CancellationException)
                block(it)
        }
}