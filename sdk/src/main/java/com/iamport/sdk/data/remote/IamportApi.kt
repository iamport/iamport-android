package com.iamport.sdk.data.remote

import com.iamport.sdk.data.chai.CHAI
import com.iamport.sdk.data.chai.request.PrepareRequest
import com.iamport.sdk.data.chai.response.ChaiPaymentStatus
import com.iamport.sdk.data.chai.response.Prepare
import com.iamport.sdk.data.chai.response.Users
import com.iamport.sdk.data.chai.response.Approve
import com.iamport.sdk.domain.utils.CONST
import retrofit2.http.*

interface IamportApi {

    @Headers("Content-Type:application/json")
    @GET("/users/pg/{${CONST.IMP_USER_CODE}}")
    suspend fun getUsers(@Path(CONST.IMP_USER_CODE) impUserCode: String): Users

    @Headers("Content-Type:application/json")
    @POST("/chai_payments/prepare")
    suspend fun postPrepare(@retrofit2.http.Body prepareRequest: PrepareRequest): Prepare

    @Headers("Content-Type:application/json")
    @GET("chai_payments/result/{${CONST.IMP_USER_CODE}}/{${CONST.IMP_UID}}?")
    suspend fun postApprove(
        @Path(CONST.IMP_USER_CODE) impUserCode: String,
        @Path(CONST.IMP_UID) impUid: String,
        @Query(CHAI.PAYMENT_ID) paymentId: String,
        @Query(CHAI.IDEMPOTENCY_KEY) idempotencyKey: String,
        @Query(CHAI.STATUS) status: ChaiPaymentStatus,
        @Query(CHAI.NATIVE) native: String
    ): Approve

    @Headers("Content-Type:application/json")
    @GET("chai_payments/result/{${CONST.IMP_USER_CODE}}/{${CONST.IMP_UID}}/{${CONST.IMP_CUSTOMER_UID}}?")
    suspend fun postApproveSubscription(
        @Path(CONST.IMP_USER_CODE) impUserCode: String,
        @Path(CONST.IMP_UID) impUid: String,
        @Path(CONST.IMP_CUSTOMER_UID) impCustomerUid: String,
        @Query(CHAI.SUBSCRIPTION_ID) subscriptionId: String,
        @Query(CHAI.IDEMPOTENCY_KEY) idempotencyKey: String,
        @Query(CHAI.STATUS) status: ChaiPaymentStatus,
        @Query(CHAI.NATIVE) native: String
    ): Approve

}