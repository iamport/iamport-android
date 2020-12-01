package com.iamport.sdk.domain.strategy.base

import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.utils.Util
import com.orhanobut.logger.Logger

interface IStrategy {
    fun init()
    suspend fun doWork(payment: Payment) {
        init()
    }

    fun sdkFinish(response: IamPortResponse?)
    fun successFinish(payment: Payment, msg: String) {
        Logger.i(msg)
        val response = Util.getSuccessResponse(payment, msg)
        sdkFinish(response)
    }

    fun failureFinish(payment: Payment, errMsg: String) {
        Logger.i(errMsg)
        val response = Util.getFailResponse(payment, errMsg)
        sdkFinish(response)
    }

}