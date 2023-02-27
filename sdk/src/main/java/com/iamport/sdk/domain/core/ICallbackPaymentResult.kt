package com.iamport.sdk.domain.core

import com.iamport.sdk.data.sdk.IamportResponse

interface ICallbackPaymentResult {
    fun result(iamPortResponse: IamportResponse?)
}