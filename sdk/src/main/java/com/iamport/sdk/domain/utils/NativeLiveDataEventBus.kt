package com.iamport.sdk.domain.utils

import androidx.lifecycle.MutableLiveData
import com.iamport.sdk.data.sdk.IamPortApprove
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment

open class NativeLiveDataEventBus {

    // 결제 완료 콜백
    val impResponse = MutableLiveData<Event<IamPortResponse?>>()

    // 차이앱
    val chaiUri = MutableLiveData<Event<String>>()

    // 웹뷰 결제 시작
    val webViewActivityPayment = MutableLiveData<Event<Payment>>()

    // 차이 결제상태 Approve
    val chaiApprove = MutableLiveData<Event<IamPortApprove>>()

    // 폴링 여부
    val isPolling = MutableLiveData<Event<Boolean>>()
}