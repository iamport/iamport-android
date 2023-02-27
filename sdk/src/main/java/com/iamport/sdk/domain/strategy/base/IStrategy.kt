package com.iamport.sdk.domain.strategy.base

import com.iamport.sdk.data.chai.response.PrepareData
import com.iamport.sdk.data.sdk.IamportResponse
import com.iamport.sdk.data.sdk.IamportRequest
import com.orhanobut.logger.Logger

interface IStrategy {
    fun init()
    suspend fun doWork(request: IamportRequest) {
        init()
    }

    fun sdkFinish(response: IamportResponse?)
    fun successFinish(request: IamportRequest, prepareData: PrepareData? = null, msg: String) {
        Logger.d(msg)
        IamportResponse.makeSuccess(request, prepareData?.impUid, msg).run {
            sdkFinish(this)
        }
    }

    fun failureFinish(request: IamportRequest, prepareData: PrepareData? = null, msg: String) {
        Logger.d(msg)
        IamportResponse.makeFail(request, prepareData?.impUid, msg).run {
            sdkFinish(this)
        }
    }

    fun successFinish(merchantUid: String, impUid: String, msg: String) {
        Logger.d(msg)
        IamportResponse.makeSuccess(merchantUid, impUid, msg).run {
            sdkFinish(this)
        }
    }

    fun failureFinish(merchantUid: String, impUid: String, msg: String) {
        Logger.d(msg)

        IamportResponse.makeFail(merchantUid, impUid, msg).run {
            sdkFinish(this)
        }
    }

}