package com.iamport.sdk.presentation.activity

import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.snackbar.Snackbar
import com.iamport.sdk.BuildConfig
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.domain.utils.BaseCoroutineScope
import com.iamport.sdk.domain.utils.UICoroutineScope
import com.iamport.sdk.presentation.viewmodel.BaseViewModel

abstract class BaseActivity<T : ViewDataBinding, R : BaseViewModel>
@JvmOverloads constructor(scope: BaseCoroutineScope = UICoroutineScope()) :
    AppCompatActivity(), BaseCoroutineScope by scope {

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

    abstract fun sdkFinish(iamPortResponse: IamPortResponse?)


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

    /**
     * 웹뷰 기본 세팅
     */
    protected fun settingsWebView(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(webView, true)
            }

            cacheMode = WebSettings.LOAD_NO_CACHE

            blockNetworkImage = false
            loadsImagesAutomatically = true

            if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = true  // api 26
            }

            useWideViewPort = false
            loadWithOverviewMode = true
            javaScriptCanOpenWindowsAutomatically = true

            domStorageEnabled = true
            loadWithOverviewMode = true
            allowContentAccess = true

            setSupportZoom(false)
            displayZoomControls = false
        }
    }

}