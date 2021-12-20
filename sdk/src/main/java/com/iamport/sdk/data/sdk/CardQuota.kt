package com.iamport.sdk.data.sdk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
data class CardQuota(val card_quota : List<Int>?) : Parcelable
//if (method === 'card' && cardQuota !== 0) {
//    params.display = {
//        card_quota: cardQuota === 1 ? [] : [cardQuota],
//    };
//}