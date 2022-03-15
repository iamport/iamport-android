package com.iamport.sampleapp.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.iamport.sampleapp.BuildConfig
import com.iamport.sampleapp.R
import com.iamport.sampleapp.ViewModel
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.utils.EventObserver


class MainActivity : AppCompatActivity() {

    private lateinit var mainLayout: View

    private val viewModel: ViewModel by viewModels()
    private val paymentFragment = PaymentFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        Iamport.init(this)

        // SDK 웹 디버깅을 위해 추가
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        }
        
        // use fragment
//        replaceFragment(paymentFragment)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, paymentFragment).commit()

        mainLayout = findViewById(R.id.container)
    }

    override fun onStart() {
        super.onStart()

        viewModel.resultCallback.observe(this, EventObserver {
            replaceFragment(PaymentResultFragment())
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("SAMPLE", "MainActivity onDestroy")
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