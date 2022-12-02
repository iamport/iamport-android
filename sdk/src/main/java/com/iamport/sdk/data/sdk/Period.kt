package com.iamport.sdk.data.sdk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * for 이니시스 정기결제 제공기간
 * 이니시스 정기결제 제공기간 옵션
 * 참조 : https://guide.iamport.kr/32498112-82c4-44cb-a23a-ef5b896ee548
 */

@Parcelize
data class Period(
    val from: String, // YYYYMMDD
    val to: String // YYYYMMDD
) : Parcelable