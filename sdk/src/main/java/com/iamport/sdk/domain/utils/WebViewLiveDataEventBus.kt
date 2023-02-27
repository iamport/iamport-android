package com.iamport.sdk.domain.utils

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.iamport.sdk.data.sdk.IamportResponse
import com.iamport.sdk.data.sdk.IamportRequest

object WebViewLiveDataEventBus {

    // 웹뷰 열기
    val openWebView = MutableLiveData<Event<IamportRequest>>()

    // 나이스 + 실시간계좌 뱅크페이 앱 결과
//    val niceTransRequestParam = MutableLiveData<Event<String>>()

    // 외부앱
    val thirdPartyUri = MutableLiveData<Event<Uri>>()

    // 로딩
    val loading = MutableLiveData<Event<Boolean>>()

    // 결제 완료 콜백
    val impResponse = MutableLiveData<Event<IamportResponse?>>()

    // 모바일 웹 모드 전용
    val changeUrl = MutableLiveData<Event<Uri>>()

}