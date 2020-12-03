package com.iamport.sdk.data.sdk

import android.os.Parcelable
import com.iamport.sdk.data.chai.response.PrepareData
import kotlinx.parcelize.Parcelize

@Parcelize
data class IamPortApprove(
    val payment: Payment,
    val prepareData: PrepareData
) : Parcelable
