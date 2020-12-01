package com.iamport.sdk.data.sdk

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// KRW / USD / EUR / JPY
// 페이팔은 USD 기본이어야 함
@Parcelize
enum class Currency : Parcelable {
    KRW, USD, EUR, JPY
}