package com.iamport.sdk.data.sdk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Payment(val userCode: String, val iamPortRequest: IamPortRequest) : Parcelable