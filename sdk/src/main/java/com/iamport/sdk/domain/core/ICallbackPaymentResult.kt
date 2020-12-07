package com.iamport.sdk.domain.core

import com.iamport.sdk.data.sdk.IamPortResponse

interface ICallbackPaymentResult {
    fun result(iamPortResponse: IamPortResponse?)
}