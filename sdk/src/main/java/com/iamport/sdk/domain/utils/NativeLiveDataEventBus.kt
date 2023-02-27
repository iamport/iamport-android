package com.iamport.sdk.domain.utils

import androidx.lifecycle.MutableLiveData
import com.iamport.sdk.data.sdk.IamportApprove
import com.iamport.sdk.data.sdk.IamportResponse
import com.iamport.sdk.data.sdk.IamportRequest

open class NativeLiveDataEventBus {

    // 결제 완료 콜백
    val impResponse = MutableLiveData<Event<IamportResponse?>>()

    // 차이앱
    val chaiUri = MutableLiveData<Event<String>>()

    // 웹뷰 결제 시작
    val webViewActivityIamportRequest = MutableLiveData<Event<IamportRequest>>()

    // 차이 결제상태 Approve
    val chaiApprove = MutableLiveData<Event<IamportApprove>>()

    // 폴링 여부
    val isPolling = MutableLiveData<Event<Boolean>>()
}