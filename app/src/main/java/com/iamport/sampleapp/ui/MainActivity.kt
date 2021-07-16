package com.iamport.sampleapp.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.iamport.sampleapp.R
import com.iamport.sampleapp.ViewModel
import com.iamport.sdk.domain.core.Iamport


class MainActivity : AppCompatActivity() {
    val viewModel: ViewModel by viewModels()

    private lateinit var mainLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        // use fragment
        replaceFragment(PaymentFragment())

        // webview mode
//        replaceFragment(WebViewModeFragment())

        mainLayout = findViewById(R.id.container)

    }

    fun replaceFragment(moveToFragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.container, moveToFragment).addToBackStack(null).commit()
    }

    fun popBackStack() {
        supportFragmentManager.popBackStack()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
//        Iamport.catchUserLeave() // TODO SDK 백그라운드 작업 중지를 위해서 onUserLeaveHint 에서 필수 호출!
    }

}