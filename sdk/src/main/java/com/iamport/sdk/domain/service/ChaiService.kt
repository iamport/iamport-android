package com.iamport.sdk.domain.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import com.iamport.sdk.R
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.Foreground


open class ChaiService : Service() {
    private val channelId = "Iamport Pamyent SDK"

    @Override
    override fun onCreate() {
        startNotification()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun channelRegister() {
        val channelName = "iamport service ch"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName,
                NotificationManager.IMPORTANCE_NONE
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun startNotification() {

        channelRegister()

        val icon = R.drawable.chuck_ic_search_white_24dp
        val title = "결제를 확인중 입니다"

        val stopIcon = R.drawable.chuck_ic_delete_white_24dp
        val stopBtnName = "중지"

        val broadcastIntent = Intent(CONST.BROADCAST_FOREGROUND_SERVICE)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val stopIntent = Intent(CONST.BROADCAST_FOREGROUND_SERVICE_STOP)
        val pendingStopIntent = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val action = Notification.Action.Builder(Icon.createWithResource("", stopIcon), stopBtnName, pendingStopIntent).build()
            Notification.Builder(this, channelId)
                .setSmallIcon(icon)  // 아이콘 셋팅
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .addAction(if (Foreground.enableForegroundServiceStopButton) action else null)
                .build()
        } else {
            val builder = Notification.Builder(this)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
            if (Foreground.enableForegroundServiceStopButton) {
                builder.addAction(stopIcon, stopBtnName, pendingStopIntent)
                    .build()
            } else {
                builder.build()
            }
        }

        startForeground(33, notification)
    }

//    protected fun stopNotification() {
//        stopForeground(true)
//        stopSelf()
//    }
}