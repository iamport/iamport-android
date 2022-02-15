package com.iamport.sdk.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.iamport.sdk.domain.utils.BaseCoroutineScope
import com.iamport.sdk.domain.utils.UICoroutineScope
import com.iamport.sdk.presentation.viewmodel.BaseViewModel

abstract class BaseActivity<R : BaseViewModel>
@JvmOverloads constructor(scope: BaseCoroutineScope = UICoroutineScope()) :
    AppCompatActivity(), BaseMain, BaseCoroutineScope by scope {

    abstract val viewModel: R
    abstract val layoutResourceId: Int

    abstract fun initStart()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layoutResourceId)
        initStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseCoroutine()
    }

}