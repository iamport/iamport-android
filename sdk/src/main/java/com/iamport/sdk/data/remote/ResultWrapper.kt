package com.iamport.sdk.data.remote

import com.iamport.sdk.data.chai.response.ErrorResponse

sealed class ResultWrapper<out T> {
    data class Success<out T>(val value: T) : ResultWrapper<T>()
    data class GenericError(val code: Int? = null, val error: ErrorResponse? = null) :
        ResultWrapper<Nothing>()

    data class NetworkError(val error: String? = null) :
        ResultWrapper<Nothing>()

    //    object NetworkError : ResultWrapper<Nothing>()
}