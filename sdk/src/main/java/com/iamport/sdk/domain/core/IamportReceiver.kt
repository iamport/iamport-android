package com.iamport.sdk.domain.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.iamport.sdk.domain.utils.CONST
import com.orhanobut.logger.Logger

class IamportReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Logger.i("IamportReceiver :: $action")

        if (action == CONST.BROADCAST_FOREGROUND_SERVICE) {
            // TODO: 적절한 처리가 필요
        }
    }
}