package com.iamport.sdk.domain.utils

import androidx.lifecycle.MutableLiveData
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment

open class NativeLiveDataEventBus {

//    var isBackground = false

    // 결제 완료 콜백
    val impResponse = MutableLiveData<Event<IamPortResponse?>>()

    // 차이앱
    val chaiUri = MutableLiveData<Event<String>>()

    // 웹뷰 결제 시작
    val webViewPayment = MutableLiveData<Event<Payment>>()
}