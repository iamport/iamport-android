package com.iamport.sampleapp.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.iamport.sampleapp.R


class MainActivity : AppCompatActivity() {

    private lateinit var mainLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        supportFragmentManager.beginTransaction().replace(R.id.container, PaymentFragment()).commitAllowingStateLoss()
        mainLayout = findViewById(R.id.container)

    }

}