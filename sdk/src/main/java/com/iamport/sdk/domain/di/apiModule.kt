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
import org.koin.core.qualifier.named
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

fun provideIamportApi(gson : Gson, client: OkHttpClient?): IamportApi {

    return Retrofit.Builder()
        .baseUrl(CONST.IAMPORT_PROD_URL)
//        .baseUrl(CONST.IAMPORT_TEST_URL)
        .addConverterFactory(GsonConverterFactory.create(gson)).apply {
            client?.let { client(it) }
        }
        .build()
        .create(IamportApi::class.java)
}

fun provideNiceApi(gson : Gson, client: OkHttpClient?): NiceApi {
    return Retrofit.Builder()
        .baseUrl("${CONST.IAMPORT_DETECT_URL}/")
        .addConverterFactory(GsonConverterFactory.create(gson)).apply {
            client?.let { client(it) }
        }
        .build()
        .create(NiceApi::class.java)
}

fun provideChaiApi(url: String, gson : Gson, client: OkHttpClient?): ChaiApi {
    return Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create(gson)).apply {
            client?.let { client(it) }
        }
        .build()
        .create(ChaiApi::class.java)
}

val httpClientModule = module {
    single(named("${CONST.KOIN_KEY}provideOkHttpClient")) { provideOkHttpClient(get()) }
}

val apiModule = module {
    single { provideIamportApi(get(named("${CONST.KOIN_KEY}Gson")), get(named("${CONST.KOIN_KEY}provideOkHttpClient")),) }
//    single { provideChaiApi(false, get(), get()) }
    single { provideNiceApi(get(named("${CONST.KOIN_KEY}Gson")), get(named("${CONST.KOIN_KEY}provideOkHttpClient")),) }
}