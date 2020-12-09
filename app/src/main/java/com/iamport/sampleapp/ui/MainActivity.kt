package com.iamport.sampleapp.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.iamport.sampleapp.R
import com.iamport.sdk.domain.core.Iamport


class MainActivity : AppCompatActivity() {

    private lateinit var mainLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        supportFragmentManager.beginTransaction().replace(R.id.container, PaymentFragment()).commitAllowingStateLoss()
        mainLayout = findViewById(R.id.container)

    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        Iamport.catchUserLeave() // TODO SDK 백그라운드 작업 중지를 위해서 onUserLeaveHint 에서 필수 호출!
    }

}