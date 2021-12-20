package com.iamport.sdk.data.sdk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * for naverpay
 */

@Parcelize
data class NaverInterface(
    val cpaInflowCode: String?,
    val naverInflowCode: String?,
    val saClickId: String?,
    val merchantCustomCode1: String?,
    val merchantCustomCode2: String?,
) : Parcelable
