package com.iamport.sdk.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.snackbar.Snackbar
import com.iamport.sdk.domain.utils.BaseCoroutineScope
import com.iamport.sdk.domain.utils.UICoroutineScope
import com.iamport.sdk.presentation.viewmodel.BaseViewModel

abstract class BaseActivity<T : ViewDataBinding, R : BaseViewModel>
@JvmOverloads constructor(scope: BaseCoroutineScope = UICoroutineScope()) :
    AppCompatActivity(), BaseMain, BaseCoroutineScope by scope {

    lateinit var viewDataBinding: T
    abstract val viewModel: R
    abstract val layoutResourceId: Int

    abstract fun initStart()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewDataBinding = DataBindingUtil.setContentView(this, layoutResourceId)
        viewDataBinding.lifecycleOwner = this

        snackbarObserving()
        initStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseCoroutine()
    }

    private fun snackbarObserving() {
        viewModel.observeSnackbarMessage(this) {
            viewDataBinding.root.run {
                it.peekContent().let {
                    Snackbar.make(this, it, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.observeSnackbarMessageStr(this) {
            viewDataBinding.root.run {
                it.peekContent().let {
                    Snackbar.make(this, it, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.observeSnackbarStrBtn(this) {
            viewDataBinding.root.run {
                it.peekContent().let {
                    Snackbar.make(this, it.first, Snackbar.LENGTH_LONG).setAction(it.second, it.third).show()
                }
            }

            viewDataBinding.root.run {
                Snackbar.make(this, "종료", Snackbar.LENGTH_LONG).setAction("확인") {
                    super.onBackPressed()
                }.show()
            }
        }
    }


}