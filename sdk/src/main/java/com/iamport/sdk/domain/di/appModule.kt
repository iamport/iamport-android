package com.iamport.sdk.domain.di

import com.google.gson.Gson
import com.iamport.sdk.domain.core.IamportReceiver
import com.iamport.sdk.domain.repository.StrategyRepository
import com.iamport.sdk.domain.strategy.base.JudgeStrategy
import com.iamport.sdk.domain.strategy.chai.ChaiStrategy
import com.iamport.sdk.domain.strategy.webview.CertificationWebViewStrategy
import com.iamport.sdk.domain.strategy.webview.NiceTransWebViewStrategy
import com.iamport.sdk.domain.strategy.webview.WebViewStrategy
import com.iamport.sdk.domain.utils.NativeLiveDataEventBus
import com.iamport.sdk.domain.utils.WebViewLiveDataEventBus
import com.iamport.sdk.presentation.viewmodel.MainViewModel
import com.iamport.sdk.presentation.viewmodel.WebViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module


@OptIn(KoinApiExtension::class)
val appModule = module {
    viewModel { MainViewModel(get(), get()) }
    viewModel { WebViewModel(get(), get()) }
    single { IamportReceiver() }
    single { Gson() }

    single { StrategyRepository() }
    single { WebViewLiveDataEventBus() }
    single { NativeLiveDataEventBus() }

    single { JudgeStrategy() }
    single { ChaiStrategy() }
    single { WebViewStrategy() }
    single { NiceTransWebViewStrategy() }
    single { CertificationWebViewStrategy() }

}