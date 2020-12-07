package com.iamport.sdk.domain.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.UrlQuerySanitizer
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.google.gson.Gson
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.PG.*
import com.iamport.sdk.data.sdk.PayMethod
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.utils.Util.observeAlways
import com.orhanobut.logger.Logger
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object Util {

    // FIXME: 11/20/20 임시로 놔둔거임
    enum class DevUserCode(val desc: String) {
        imp55870459("kicc"), imp96304110("bingbong"), imp60029475("moblisans");

        companion object {
            fun getUserCodes(): List<String> {
                return values().map { "${it.desc} (${it.name})" }.toList()
            }
        }
    }

    private val defaultPayMethod =
        setOf(PayMethod.card, PayMethod.vbank, PayMethod.trans, PayMethod.phone)

    fun convertPayMethodNames(pg: PG): List<String> {
        return getMappingPayMethod(pg).map { it.getPayMethodName() }.toList()
    }


    /**
     * pg사별 지원하는 결제수단
     */
    fun getMappingPayMethod(pg: PG): Set<PayMethod> {
        return when (pg) {
            html5_inicis -> defaultPayMethod + setOf(
                PayMethod.samsung,
                PayMethod.kpay,
                PayMethod.cultureland,
                PayMethod.smartculture,
                PayMethod.happymoney
            )
            kcp -> defaultPayMethod + setOf(PayMethod.samsung)
            kcp_billing, kakaopay, kakao,
            paypal, payco, smilepay, alipay ->
                setOf(PayMethod.card)
            uplus -> defaultPayMethod + setOf(
                PayMethod.cultureland,
                PayMethod.smartculture,
                PayMethod.booknlife
            )
            danal -> setOf(PayMethod.phone)
            mobilians -> setOf(PayMethod.card, PayMethod.phone)
            settle -> setOf(PayMethod.vbank)
            chai, payple -> setOf(PayMethod.trans)
            jtnet, nice, danal_tpay, kicc,
            eximbay, naverco, naverpay -> defaultPayMethod
            else -> defaultPayMethod
        }
    }

    /**
     * uri 쿼리 파싱해서 IamPortResponse 가져오기
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun getQueryStringToImpResponse(uri: Uri, gson: Gson): IamPortResponse {
        val uriStr = URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8.toString())
        val sanitizer = UrlQuerySanitizer(uriStr)
        val queryMap = HashMap<String, String>()
        sanitizer.parameterList.forEach {
            queryMap[it.mParameter] = uri.getQueryParameter(it.mParameter).toString()
        }
        return gson.fromJson(gson.toJson(queryMap), IamPortResponse::class.java)
    }

    /**
     * 플레이 스토어 이동 주소
     */
    fun getMarketId(pkg: String): String {
        return "${CONST.PAYMENT_PLAY_STORE_URL}$pkg"
    }


    // 네트워크 연결 체크
    @Suppress("DEPRECATION")
    fun isInternetAvailable(context: Context?): Boolean {
        var result = false
        if (context == null) {
            Logger.e("isInternetAvailable :: Not Found context")
            return result
        }
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                getNetworkCapabilities(activeNetwork)?.run {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                }
            }
        } else {
            cm?.run {
                activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = true
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = true
                    }
                }
            }
        }
        return result
    }


    fun getOrZeroString(value: String?): String {
        return value ?: "0"
    }

    fun getOrEmpty(value: String?): String {
        return value ?: CONST.EMPTY_STR
    }

    // LiveData 백그라운드 옵저빙

    internal fun <T> LiveData<T>.observeAlways(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        val lifecycleObserver = object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                removeObserver(observer)
                lifecycleOwner.lifecycle.removeObserver(this)
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        observeForever(observer)
    }

}