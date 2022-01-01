package com.iamport.sdk.data.sdk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * for naverpay
 */

@Parcelize
@Serializable
data class Card(
    val direct: Direct
) : Parcelable


@Parcelize
@Serializable
data class Direct(var code: String, var quota: Int? = null
) : Parcelable