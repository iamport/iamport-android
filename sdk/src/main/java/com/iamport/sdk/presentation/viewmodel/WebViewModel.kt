package com.iamport.sdk.presentation.viewmodel

import android.net.Uri
import android.webkit.WebViewClient
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.di.IamportKoinComponent
import com.iamport.sdk.domain.repository.StrategyRepository
import com.iamport.sdk.domain.strategy.webview.NiceTransWebViewStrategy
import com.iamport.sdk.domain.utils.Event
import com.iamport.sdk.domain.utils.WebViewLiveDataEventBus
import com.orhanobut.logger.Logger.d
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class WebViewModel(private val bus: WebViewLiveDataEventBus, private val repository: StrategyRepository) : BaseViewModel(), IamportKoinComponent {

    override fun onCleared() {
        d("onCleared")
        super.onCleared()
    }

    /**
     * 결제 데이터
     */
    fun payment(): LiveData<Event<Payment>> {
        return bus.webViewPayment
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
    fun niceTransRequestParam(): LiveData<Event<String>> {
        return bus.niceTransRequestParam
    }

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
     * 로딩 프로그래스
     */
    fun loading(): LiveData<Event<Boolean>> {
        return bus.loading
    }


    /**
     * PG(nice or 비nice) 따라 webview client 가져오기
     */
    fun getWebViewClient(payment: Payment): WebViewClient {
        return repository.getWebViewClient(payment)
    }

    /**
     * activity 에서 결제 요청
     */
    fun startPayment(payment: Payment) {
        bus.webViewPayment.postValue(Event(payment))
    }

    /**
     * 뱅크페이 결과 처리
     */
    fun processBankPayPayment(resPair: Pair<String, String>) {
        repository.getNiceTransWebViewClient().processBankPayPayment(resPair)
    }

    fun getNiceTransWebViewClient(): NiceTransWebViewStrategy {
        return repository.getNiceTransWebViewClient()
    }

    /**
     * 결제 요청
     */
    fun requestPayment(payment: Payment) {
        viewModelScope.launch {
            repository.getWebViewStrategy(payment).doWork(payment)
        }
    }
}