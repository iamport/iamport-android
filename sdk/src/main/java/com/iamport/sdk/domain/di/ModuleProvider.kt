package com.iamport.sdk.domain.di

import com.google.gson.Gson
import com.iamport.sdk.BuildConfig
import com.iamport.sdk.data.remote.ChaiApi
import com.iamport.sdk.data.remote.IamportApi
import com.iamport.sdk.data.remote.NiceApi
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.JsNativeInterface
import com.iamport.sdk.domain.core.IamportReceiver
import com.iamport.sdk.domain.repository.StrategyRepository
import com.iamport.sdk.domain.strategy.base.JudgeStrategy
import com.iamport.sdk.domain.strategy.chai.ChaiStrategy
import com.iamport.sdk.domain.strategy.webview.IamPortMobileModeWebViewClient
import com.iamport.sdk.domain.strategy.webview.WebViewStrategy
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.NativeLiveDataEventBus
import com.iamport.sdk.domain.utils.WebViewLiveDataEventBus
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class ModuleProvider {

    companion object {
        val gson by lazy { Gson() }

        // BroadcastReceiver
        val iamportReceiver by lazy { IamportReceiver() }

        // 이벤트 버스들
        val nativeLiveDataEventBus by lazy { NativeLiveDataEventBus() } // 네이티브 연동시 필요한 livedata
        val webViewLiveDataEventBus by lazy { WebViewLiveDataEventBus() } // 웹뷰(보통) 연동시 필요한 livedata

        // 아임포트 서버 API
        private val iamportApi: IamportApi by lazy { apiModule.provideIamportApi(gson, apiModule.provideOkHttpClient()) }
        val apiModule by lazy { ApiModule() }

        // 각 strategy 들
        private val judgeStrategy by lazy { JudgeStrategy(iamportApi) } // GET 으로 유저정보 찔러봄
        private val chaiStrategy by lazy { ChaiStrategy(iamportApi) } // 결제 중 BG 폴링하는 차이 전략
        private val webViewStrategy by lazy { WebViewStrategy() } // webview 사용하는 pg
        private val mobileWebModeStrategy by lazy { IamPortMobileModeWebViewClient() } // webview 사용하는 pg

        // strategy를 주입받아 사용하는 repository
        val strategyRepository by lazy { StrategyRepository(judgeStrategy, chaiStrategy, webViewStrategy, mobileWebModeStrategy) } // strategy의 repository
    }

    class ApiModule {

        private fun retrofitBuilder(urlStr: String): Retrofit.Builder {
            return Retrofit.Builder()
                .baseUrl(urlStr)
        }

        fun provideOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.MINUTES)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS).apply {
                    if (BuildConfig.DEBUG) {
                        addInterceptor(HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        })
                    }
                }
                .build()
        }

        fun provideIamportApi(gson: Gson, client: OkHttpClient): IamportApi {
            return retrofitBuilder(CONST.IAMPORT_PROD_URL)
                .addConverterFactory(GsonConverterFactory.create(gson)).apply {
                    this.client(client)
                }
                .build()
                .create(IamportApi::class.java)
        }

        @Deprecated(message = "현재 pg nice 관련해서 특별한 처리를 하지 않음")
        fun provideNiceApi(gson: Gson, client: OkHttpClient): NiceApi {
            return retrofitBuilder("${CONST.IAMPORT_DETECT_URL}/")
                .addConverterFactory(GsonConverterFactory.create(gson)).apply {
                    this.client(client)
                }
                .build()
                .create(NiceApi::class.java)
        }

        fun provideChaiApi(url: String): ChaiApi {
            return retrofitBuilder(url)
                .addConverterFactory(GsonConverterFactory.create(gson)).apply {
                    this.client(provideOkHttpClient())
                }
                .build()
                .create(ChaiApi::class.java)
        }

        fun provideJsNativeInterface(payment: Payment, evaluateJS: ((String) -> Unit)): JsNativeInterface {
            return JsNativeInterface(payment, gson, webViewLiveDataEventBus, evaluateJS)
        }

    }

}
