package com.iamport.sdk.domain.strategy.base

import com.iamport.sdk.data.chai.response.PrepareData
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.orhanobut.logger.Logger

interface IStrategy {
    fun init()
    suspend fun doWork(payment: Payment) {
        init()
    }

    fun sdkFinish(response: IamPortResponse?)
    fun successFinish(payment: Payment, prepareData: PrepareData? = null, msg: String) {
        Logger.d(msg)
        IamPortResponse.makeSuccess(payment, prepareData?.impUid, msg).run {
            sdkFinish(this)
        }
    }

    fun failureFinish(payment: Payment, prepareData: PrepareData? = null, msg: String) {
        Logger.d(msg)
        IamPortResponse.makeFail(payment, prepareData?.impUid, msg).run {
            sdkFinish(this)
        }
    }

    fun successFinish(merchantUid: String, impUid: String, msg: String) {
        Logger.d(msg)
        IamPortResponse.makeSuccess(merchantUid, impUid, msg).run {
            sdkFinish(this)
        }
    }

    fun failureFinish(merchantUid: String, impUid: String, msg: String) {
        Logger.d(msg)

        IamPortResponse.makeFail(merchantUid, impUid, msg).run {
            sdkFinish(this)
        }
    }

}