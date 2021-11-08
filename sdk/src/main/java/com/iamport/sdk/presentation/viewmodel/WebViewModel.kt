package com.iamport.sdk.presentation.viewmodel

import android.net.Uri
import android.webkit.WebViewClient
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.di.IamportKoinComponent
import com.iamport.sdk.domain.repository.StrategyRepository
import com.iamport.sdk.domain.strategy.webview.IamPortMobileModeWebViewClient
import com.iamport.sdk.domain.utils.Event
import com.iamport.sdk.domain.utils.WebViewLiveDataEventBus
import com.orhanobut.logger.Logger.d
import kotlinx.coroutines.launch

class WebViewModel(private val repository: StrategyRepository) : BaseViewModel(), IamportKoinComponent {

    private val bus: WebViewLiveDataEventBus by lazy { WebViewLiveDataEventBus }

    override fun onCleared() {
        d("onCleared")
        repository.init()
        super.onCleared()
    }

    /**
     * 오픈 웹뷰
     */
    fun openWebView(): LiveData<Event<Payment>> {
        return bus.openWebView
    }

    /**
     * 뱅크페이 외부앱 열기
     */
//    fun niceTransRequestParam(): LiveData<Event<String>> {
//        return bus.niceTransRequestParam
//    }

    /**
     * 외부앱 열기
     */
    fun thirdPartyUri(): LiveData<Event<Uri>> {
        return bus.thirdPartyUri
    }

    /**
     * 결제 결과 콜백 및 종료
     */
    fun impResponse(): LiveData<Event<IamPortResponse?>> {
        return bus.impResponse
    }

    /**
     * 모바일 웹 모드 url 변경
     */
    fun changeUrl(): LiveData<Event<Uri>> {
        return bus.changeUrl
    }


    /**
     * 로딩 프로그래스
     */
    fun loading(): LiveData<Event<Boolean>> {
        return bus.loading
    }


    /**
     * PG(nice or 비nice) 따라 webview client 가져오기
     */
    fun getWebViewClient(): WebViewClient {
        return repository.getWebViewClient()
    }

    /**
     * 뱅크페이 결과 처리
     */
//    fun processBankPayPayment(resPair: Pair<String, String>) {
//        repository.getNiceTransWebViewClient().processBankPayPayment(resPair)
//    }

    /**
     * MobileWebMode 뱅크페이 결과 처리
     */
//    fun mobileModeProcessBankPayPayment(resPair: Pair<String, String>) {
//        getMobileWebModeClient().processBankPayPayment(resPair)
//    }

    /**
     * MobileWebMode WebViewClient
     */
    fun getMobileWebModeClient(): IamPortMobileModeWebViewClient {
        return repository.getMobileWebModeClient()
    }

    /**
     * MobileWebMode WebViewClient
     */
    fun updateMobileWebModeClient(client: IamPortMobileModeWebViewClient) {
        return repository.updateMobileWebModeClient(client)
    }

    /**
     * 결제 요청
     */
    fun requestPayment(payment: Payment) {
        viewModelScope.launch {
            repository.getWebViewStrategy().doWork(payment)
        }
    }
}