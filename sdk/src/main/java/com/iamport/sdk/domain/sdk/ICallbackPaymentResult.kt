package com.iamport.sdk.domain.sdk

import com.iamport.sdk.data.sdk.IamPortResponse

interface ICallbackPaymentResult {
    fun result(iamPortResponse: IamPortResponse?)
}