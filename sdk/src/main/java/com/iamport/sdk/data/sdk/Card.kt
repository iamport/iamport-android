package com.iamport.sdk.data.sdk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * for naverpay
 */

@Parcelize
data class Card(
    val direct: Direct
) : Parcelable


@Parcelize
data class Direct(var code: String, var quota: Int? = null
) : Parcelable