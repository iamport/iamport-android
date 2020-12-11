package com.iamport.sdk.domain.di

import android.content.Context
import com.google.gson.Gson
import com.iamport.sdk.BuildConfig
import com.iamport.sdk.data.remote.ChaiApi
import com.iamport.sdk.data.remote.IamportApi
import com.iamport.sdk.data.remote.NiceApi
import com.iamport.sdk.domain.repository.StrategyRepository
import com.iamport.sdk.domain.strategy.base.BaseStrategy
import com.iamport.sdk.domain.strategy.base.BaseWebViewStrategy
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


fun provideOkHttpClient(context: Context): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.MINUTES)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(ChuckInterceptor(context))
        .build()
}

fun provideIamportApi(gson: Gson, client: OkHttpClient): IamportApi {

    return Retrofit.Builder()
        .baseUrl(CONST.IAMPORT_PROD_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()
        .create(IamportApi::class.java)
}

fun provideNiceApi(gson: Gson, client: OkHttpClient): NiceApi {
    return Retrofit.Builder()
        .baseUrl("${CONST.IAMPORT_DUMMY_URL}/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()
        .create(NiceApi::class.java)
}

fun provideChaiApi(gson: Gson, client: OkHttpClient): ChaiApi {
    return Retrofit.Builder()
//        .baseUrl(if (BuildConfig.DEBUG) CONST.CHAI_SERVICE_STAGING_URL else CONST.CHAI_SERVICE_URL)
        .baseUrl(CONST.CHAI_SERVICE_STAGING_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()
        .create(ChaiApi::class.java)
}

@OptIn(KoinApiExtension::class)
val appModule = module {
    viewModel { WebViewModel(get(), get()) }
    single { Gson() }
    single { provideOkHttpClient(get()) }

    single { StrategyRepository() }
    single { WebViewLiveDataEventBus() }
    single { NativeLiveDataEventBus() }

    single { JudgeStrategy() }
    single { ChaiStrategy() }
    single { WebViewStrategy() }
    single { NiceTransWebViewStrategy() }

    single { provideIamportApi(get(), get()) }
    single { provideChaiApi(get(), get()) }
    single { provideNiceApi(get(), get()) }

}