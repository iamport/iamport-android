package com.iamport.sdk.domain.strategy.base

import com.iamport.sdk.data.chai.response.UserData
import com.iamport.sdk.data.chai.response.Users
import com.iamport.sdk.data.remote.ApiHelper
import com.iamport.sdk.data.remote.IamportApi
import com.iamport.sdk.data.remote.ResultWrapper
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.IamportRequest
import com.iamport.sdk.domain.di.IamportKoinComponent
import com.iamport.sdk.domain.utils.Constant
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

    suspend fun judge(request: IamportRequest, ignoreNative: Boolean = false): Triple<JudgeKinds, UserData?, IamportRequest> {
        this.ignoreNative = ignoreNative

//        * 1. IMP 서버에 유저 정보 요청해서 chai id 얻음
        val userDataList: ArrayList<UserData>? = when (val response = apiGetUsers(request.userCode)) {
            is ResultWrapper.NetworkError -> {
                failureFinish(request, msg = "NetworkError ${response.error}")
                null
            }
            is ResultWrapper.GenericError -> {
                failureFinish(request, msg = "GenericError ${response.code} ${response.error}")
                null
            }

            is ResultWrapper.Success -> {
                response.value.run {
                    if (code == 0) {
                        data
                    } else {
                        failureFinish(request, msg = msg)
                        null
                    }
                }
            }
        }

        // 유저 PG 정보 아예 없으면 실패처리
        if (userDataList.isNullOrEmpty()) {
            failureFinish(request, msg = "Not found PG [ ${request.iamportPayment?.pg} ] and any PG in your info.")
            return Triple(JudgeKinds.ERROR, null, request)
        }

        // 1. 본인인증의 경우 판단 (현재 있는지 없는지만 판단)

        when (request.getStatus()) {
            IamportRequest.STATUS.CERT -> {
                val defCertUser = userDataList.find {
                    it.pg_provider != null && it.type == Constant.USER_TYPE_CERTIFICATION
                } ?: run {
                    failureFinish(request = request, msg = "본인인증 설정 또는 가입을 먼저 해주세요.")
                    return Triple(JudgeKinds.ERROR, null, request)
                }

                return Triple(JudgeKinds.CERT, defCertUser, request)
            }
            IamportRequest.STATUS.ERROR -> {
                failureFinish(request = request, msg = "judge :: payment status ERROR")
                return Triple(JudgeKinds.ERROR, null, request)
            }
        }

        // 2. 결제요청의 경우 판단
        val defUser = findDefaultUserData(userDataList) ?: run {
            failureFinish(request, msg = "Not found Default PG. All PG empty.")
            return Triple(JudgeKinds.ERROR, null, request)
        }

        Logger.d("userDataList :: $userDataList")
        val split = request.iamportPayment?.pg?.split(".") ?: run {
            failureFinish(request = request, msg = "Not found My PG.")
            return Triple(JudgeKinds.ERROR, null, request)
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
                    getPgTriple(defUser, replacePG(pg, request))
                }
            } ?: run {
                Triple(JudgeKinds.ERROR, null, request)
            }

            else -> getPgTriple(user, request)
        }
    }

    private fun findDefaultUserData(userDataList: ArrayList<UserData>): UserData? {
        return userDataList.find { it.pg_provider != null && it.type == Constant.USER_TYPE_PAYMENT }
    }

    /**
     * pg 정보 값 가져옴 first : 타입, second : pg유저, third : 결제 요청 데이터
     */
    private fun getPgTriple(user: UserData, request: IamportRequest): Triple<JudgeKinds, UserData?, IamportRequest> {
        return when (user.pg_provider?.let { PG.convertPG(it) }) {
            PG.chai -> {
                if (ignoreNative) { // ignoreNative 인 경우 webview strategy 가 동작하기 위하여
                   return Triple(JudgeKinds.WEB, user, request)
                }
                Triple(JudgeKinds.CHAI, user, request)
            }
            else -> Triple(JudgeKinds.WEB, user, request)
        }
    }

    /**
     * payment PG 를 default PG 로 수정함
     */
    private fun replacePG(pg: PG, request: IamportRequest): IamportRequest {
        val iamPortRequest = request.iamportPayment?.copy(pg = pg.makePgRawName())
        return request.copy(iamportPayment = iamPortRequest)
    }

}
