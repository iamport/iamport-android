package com.iamport.sdk.domain.di

import android.content.Context
import com.google.gson.Gson
import com.iamport.sdk.data.remote.ChaiApi
import com.iamport.sdk.data.remote.IamportApi
import com.iamport.sdk.data.remote.NiceApi
import com.iamport.sdk.domain.core.IamportReceiver
import com.iamport.sdk.domain.repository.StrategyRepository
import com.iamport.sdk.domain.strategy.base.JudgeStrategy
import com.iamport.sdk.domain.strategy.chai.ChaiStrategy
import com.iamport.sdk.domain.strategy.webview.NiceTransWebViewStrategy
import com.iamport.sdk.domain.strategy.webview.WebViewStrategy
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.NativeLiveDataEventBus
import com.iamport.sdk.domain.utils.WebViewLiveDataEventBus
import com.iamport.sdk.presentation.viewmodel.WebViewModel
import com.readystatesoftware.chuck.ChuckInterceptor
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


@OptIn(KoinApiExtension::class)
val appModule = module {
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

}