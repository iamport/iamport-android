package com.iamport.sampleapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.utils.CONST

/**
 * SDK 로 부터 전달받는 브로드캐스트 리시버
 */
class MerchantReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {
            CONST.BROADCAST_FOREGROUND_SERVICE -> {
                Log.d("SAMPLE", "MerchantReceiver BROADCAST_FOREGROUND_SERVICE")
                // 포그라운드 서비스 클릭시 호출
                // TODO: 적절한 처리가 필요
            }
            CONST.BROADCAST_FOREGROUND_SERVICE_STOP -> {
                Log.d("SAMPLE", "MerchantReceiver BROADCAST_FOREGROUND_SERVICE_STOP")
                // 포그라운드 서비스 중지버튼 클릭시 호출
                // TODO: 적절한 처리가 필요
                Iamport.failFinish()
            }
        }
    }
}