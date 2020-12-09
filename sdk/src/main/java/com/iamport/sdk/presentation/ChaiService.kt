//package com.iamport.sdk.presentation
//
//import android.app.*
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.os.IBinder
//import android.widget.RemoteViews
//import com.readystatesoftware.chuck.internal.ui.MainActivity
//
//class ChaiService : Service() {
//    private val channelId = "channelId test"
//
//
//    @Override
//    override fun onCreate() {
//        startNotification()
//    }
//
//    override fun onBind(p0: Intent?): IBinder? {
//        return null
//    }
//
//    // 채널을 등록하는 함수.
//    private fun channelRegister() {
//        val channelName = "service channel name"
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId, channelName,
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            manager.createNotificationChannel(channel)
//        }
//    }
//
//    // foreground 시작하는 함수.
//    private fun startNotification() {
//        channelRegister()
//        // PendingIntent 입니다.
//        val contentIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)
//        // Foreground Service의 layout입니다.
////        val view = RemoteViews(packageName, R.layout.service_first)
//        // notification 셋팅
//        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Notification.Builder(this, channelId)
////                .setSmallIcon(R.drawable.ic_launcher_foreground)  // 아이콘 셋팅
////                .setCustomContentView(view)       // 레이아웃 셋팅
//                .setContentIntent(contentIntent)  // pendingIntent 클릭시 화면 전환을 위해
//                .build()
//        } else {
//            //TODO("VERSION.SDK_INT < O")
//            Notification.Builder(this)
////                .setSmallIcon(R.drawable.ic_launcher_foreground)  // 아이콘 셋팅
////                .setContent(view)                 // 레이아웃 셋팅
//                .setContentIntent(contentIntent)  // pendingIntent 클릭시 화면 전환을 위해
//                .build()
//        }
//
//        // Foreground 시작하는 코드
//        startForeground(1, notification)
//    }
//}