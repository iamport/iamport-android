package com.iamport.sdk.domain.di

import android.content.Context
import com.google.gson.Gson
import com.iamport.sdk.BuildConfig
import com.iamport.sdk.data.remote.ChaiApi
import com.iamport.sdk.data.remote.IamportApi
import com.iamport.sdk.data.remote.NiceApi
import com.iamport.sdk.domain.utils.CONST
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


fun provideOkHttpClient(context: Context?): OkHttpClient? {
    return context?.let {
        OkHttpClient.Builder()
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
    } ?: run { null }
}

fun provideIamportApi(gson: Gson, client: OkHttpClient?): IamportApi {

    return Retrofit.Builder()
        .baseUrl(CONST.IAMPORT_PROD_URL)
        .addConverterFactory(GsonConverterFactory.create(gson)).apply {
            client?.let { client(it) }
        }
        .build()
        .create(IamportApi::class.java)
}

fun provideNiceApi(gson: Gson, client: OkHttpClient?): NiceApi {
    return Retrofit.Builder()
        .baseUrl("${CONST.IAMPORT_DETECT_URL}/")
        .addConverterFactory(GsonConverterFactory.create(gson)).apply {
            client?.let { client(it) }
        }
        .build()
        .create(NiceApi::class.java)
}

fun provideChaiApi(isStaging: Boolean, gson: Gson, client: OkHttpClient?): ChaiApi {
    return Retrofit.Builder()
        .baseUrl(if (isStaging) CONST.CHAI_SERVICE_STAGING_URL else CONST.CHAI_SERVICE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson)).apply {
            client?.let { client(it) }
        }
        .build()
        .create(ChaiApi::class.java)
}

@OptIn(KoinApiExtension::class)
val httpClientModule = module {
    single { provideOkHttpClient(get()) }
}

@OptIn(KoinApiExtension::class)
val apiModule = module {
    single { provideIamportApi(get(), get()) }
//    single { provideChaiApi(false, get(), get()) }
    single { provideNiceApi(get(), get()) }
}