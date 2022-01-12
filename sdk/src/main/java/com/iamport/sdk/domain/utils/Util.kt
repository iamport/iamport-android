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
import com.iamport.sdk.BuildConfig
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.PG.*
import com.iamport.sdk.data.sdk.PayMethod
import com.orhanobut.logger.Logger
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object Util {

    // FIXME: 11/20/20 임시로 놔둔거임
    enum class DevUserCode(val desc: String) {
        imp96304110("bingbong 테스트"), imp55870459("kicc 테스트"), imp60029475("mobilians 테스트");

        companion object {
            fun getUserCodes(): List<String> {
                return values().map { "${it.desc} (${it.name})" }.toList()
            }
        }
    }

    enum class SampleUserCode(val desc: String) {
        imp19424728("default 테스트"),
        iamport("관리자 체험하기 계정"),
        imp10391932("kakao 테스트"),
        imp09350031("paypal 테스트"),
        imp60029475("mobilians 테스트"),
        imp41073887("naverco, naverpay 테스트"),
        imp49241793("smilepay 테스트"),
        imp37739582("chai 테스트"),
        imp87936124("alipay 테스트"),
        imp02690184("smartro 테스트"),
        imp42284830("payple 테스트"),
        imp46277621("settlebank 테스트");

        companion object {
            fun getUserCodes(): List<String> {
                return values().map { "${it.desc} (${it.name})" }.toList()
            }
        }
    }

    fun getUserCodeList(): List<String> {
        return if (BuildConfig.DEBUG) {
            DevUserCode.getUserCodes() + SampleUserCode.getUserCodes()
        } else {
            SampleUserCode.getUserCodes()
        }
    }

    fun getUserCode(position: Int): String {
        val sampleUserCodeValues = SampleUserCode.values()
        return if (BuildConfig.DEBUG) {
            val devUserCodeValues = DevUserCode.values()
            if (position >= devUserCodeValues.size) {
                sampleUserCodeValues[position - devUserCodeValues.size].name
            } else {
                devUserCodeValues[position].name
            }
        } else {
            sampleUserCodeValues[position].name
        }
    }

    inline fun <reified T : Enum<T>> printAllValues() {
        print(enumValues<T>().joinToString { it.name })
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
            kcp -> defaultPayMethod + setOf(PayMethod.samsung, PayMethod.naverpay)
            kcp_billing, kakaopay,
            paypal, payco, smilepay, alipay, settle_firm ->
                setOf(PayMethod.card)
            uplus -> defaultPayMethod + setOf(
                PayMethod.cultureland,
                PayMethod.smartculture,
                PayMethod.booknlife
            )
            tosspay -> setOf(
                PayMethod.card,
                PayMethod.trans
            )
            danal -> setOf(PayMethod.phone)
            mobilians -> setOf(PayMethod.card, PayMethod.phone)
            settle -> setOf(PayMethod.card, PayMethod.vbank)
            chai, payple -> setOf(PayMethod.trans)
            eximbay -> setOf(
                PayMethod.card,
                PayMethod.unionpay,
                PayMethod.alipay,
                PayMethod.tenpay,
                PayMethod.wechat,
                PayMethod.molpay,
                PayMethod.paysbuy
            )
            jtnet, nice, danal_tpay, kicc -> defaultPayMethod
            /*naverco,*/ naverpay -> setOf(PayMethod.card)
            smartro -> setOf(PayMethod.card, PayMethod.vbank, PayMethod.trans)
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
                    Logger.d(this.toString())
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
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

//    fun versionName(context: Context?, name: String? = null): String {
//        return context?.run {
//            packageManager.getPackageInfo(name ?: packageName, 0)?.versionName
//        } ?: kotlin.run { CONST.EMPTY_STR }
//    }

    fun versionCode(context: Context?, name: String? = null): Number {
        return context?.run {
            packageManager.getPackageInfo(name ?: packageName, 0)?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    it.longVersionCode
                } else {
                    it.versionCode
                }
            }
        } ?: kotlin.run { 0L }

    }

    fun getRedirectUrl(str: String): String {
        return "${CONST.IAMPORT_DETECT_SCHEME}${CONST.IAMPORT_DETECT_ADDRESS}/${str}"
    }

}
