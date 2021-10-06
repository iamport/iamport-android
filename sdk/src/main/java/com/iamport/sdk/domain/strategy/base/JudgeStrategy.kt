package com.iamport.sdk.domain.strategy.base

import com.iamport.sdk.data.chai.response.UserData
import com.iamport.sdk.data.chai.response.Users
import com.iamport.sdk.data.remote.ApiHelper
import com.iamport.sdk.data.remote.IamportApi
import com.iamport.sdk.data.remote.ResultWrapper
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.di.IamportKoinComponent
import com.iamport.sdk.domain.utils.CONST
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import org.koin.core.component.inject

class JudgeStrategy : BaseStrategy(), IamportKoinComponent {

    // 유저 정보 판단 결과 타입
    enum class JudgeKinds {
        CHAI, WEB, CERT, ERROR
    }

    private val iamportApi: IamportApi by inject() // 아임포트 서버 API
    private var ignoreNative = false

    // #1 API imp uid 에 따른 유저정보 가져오기
    private suspend fun apiGetUsers(userCode: String): ResultWrapper<Users> {
        Logger.d("try apiGetUsers")
        return ApiHelper.safeApiCall(Dispatchers.IO) { iamportApi.getUsers(userCode) }
    }

    suspend fun judge(payment: Payment, ignoreNative: Boolean = false): Triple<JudgeKinds, UserData?, Payment> {
        this.ignoreNative = ignoreNative

//        * 1. IMP 서버에 유저 정보 요청해서 chai id 얻음
        val userDataList: ArrayList<UserData>? = when (val response = apiGetUsers(payment.userCode)) {
            is ResultWrapper.NetworkError -> {
                failureFinish(payment, msg = "NetworkError ${response.error}")
                null
            }
            is ResultWrapper.GenericError -> {
                failureFinish(payment, msg = "GenericError ${response.code} ${response.error}")
                null
            }

            is ResultWrapper.Success -> {
                response.value.run {
                    if (code == 0) {
                        data
                    } else {
                        failureFinish(payment, msg = msg)
                        null
                    }
                }
            }
        }

        // 유저 PG 정보 아예 없으면 실패처리
        if (userDataList.isNullOrEmpty()) {
            failureFinish(payment, msg = "Not found PG [ ${payment.iamPortRequest?.pg} ] and any PG in your info.")
            return Triple(JudgeKinds.ERROR, null, payment)
        }

        // 1. 본인인증의 경우 판단 (현재 있는지 없는지만 판단)

        when (payment.getStatus()) {
            Payment.STATUS.CERT -> {
                val defCertUser = userDataList.find {
                    it.pg_provider != null && it.type == CONST.USER_TYPE_CERTIFICATION
                } ?: run {
                    failureFinish(payment = payment, msg = "본인인증 설정 또는 가입을 먼저 해주세요.")
                    return Triple(JudgeKinds.ERROR, null, payment)
                }

                return Triple(JudgeKinds.CERT, defCertUser, payment)
            }
            Payment.STATUS.ERROR -> {
                failureFinish(payment = payment, msg = "judge :: payment status ERROR")
                return Triple(JudgeKinds.ERROR, null, payment)
            }
        }

        // 2. 결제요청의 경우 판단
        val defUser = findDefaultUserData(userDataList) ?: run {
            failureFinish(payment, msg = "Not found Default PG. All PG empty.")
            return Triple(JudgeKinds.ERROR, null, payment)
        }

        Logger.d("userDataList :: $userDataList")
        val split = payment.iamPortRequest?.pg?.split(".") ?: run {
            failureFinish(payment = payment, msg = "Not found My PG.")
            return Triple(JudgeKinds.ERROR, null, payment)
        }

        val myPg = split[0]
        val user = userDataList.find {
            if (split.size > 1) {
                it.pg_provider == myPg && it.pg_id == split[1]
            } else {
                it.pg_provider == myPg
            }
        }
        Logger.d("user :: $user")

        return when (user) {
            null -> defUser.pg_provider?.let {
                PG.convertPG(it)?.let { pg ->
                    getPgTriple(defUser, replacePG(pg, payment))
                }
            } ?: run {
                Triple(JudgeKinds.ERROR, null, payment)
            }

            else -> getPgTriple(user, payment)
        }
    }

    private fun findDefaultUserData(userDataList: ArrayList<UserData>): UserData? {
        return userDataList.find { it.pg_provider != null && it.type == CONST.USER_TYPE_PAYMENT }
    }

    /**
     * pg 정보 값 가져옴 first : 타입, second : pg유저, third : 결제 요청 데이터
     */
    private fun getPgTriple(user: UserData, payment: Payment): Triple<JudgeKinds, UserData?, Payment> {
        return when (user.pg_provider?.let { PG.convertPG(it) }) {
            PG.chai -> {
                if (ignoreNative) { // ignoreNative 인 경우 webview strategy 가 동작하기 위하여
                   return Triple(JudgeKinds.WEB, user, payment)
                }
                Triple(JudgeKinds.CHAI, user, payment)
            }
            else -> Triple(JudgeKinds.WEB, user, payment)
        }
    }

    /**
     * payment PG 를 default PG 로 수정함
     */
    private fun replacePG(pg: PG, payment: Payment): Payment {
        val iamPortRequest = payment.iamPortRequest?.copy(pg = pg.makePgRawName())
        return payment.copy(iamPortRequest = iamPortRequest)
    }

}
