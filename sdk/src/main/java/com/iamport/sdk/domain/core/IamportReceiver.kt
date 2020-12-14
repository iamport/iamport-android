package com.iamport.sdk.domain.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.iamport.sdk.domain.utils.CONST
import com.orhanobut.logger.Logger

class IamportReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Logger.d("IamportReceiver :: $action")

        // 포그라운드 서비스 클릭시 호출 됩니다.
        if (action == CONST.BROADCAST_FOREGROUND_SERVICE) {
            // TODO: 적절한 처리가 필요
        }

        // 포그라운드 서비스의 중지 버튼 클릭시 호출 됩니다.
        if (action == CONST.BROADCAST_FOREGROUND_SERVICE_STOP) {
        }

    }
}