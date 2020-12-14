package com.iamport.sampleapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.utils.CONST
import com.orhanobut.logger.Logger

/**
 * SDK 로 부터 전달받는 브로드캐스트 리시버
 */
class MerchantReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Logger.i("MerchantReceiver :: $action")

        // 포그라운드 서비스 클릭시 호출 됩니다.
        if (action == CONST.BROADCAST_FOREGROUND_SERVICE) {
            // TODO: 적절한 처리가 필요
//            Iamport.close() // 예시입니다. 단순 SDK 종료.
            Iamport.failFinish() // 예시입니다. 데이터가 있을시 실패 response
        }
    }
}