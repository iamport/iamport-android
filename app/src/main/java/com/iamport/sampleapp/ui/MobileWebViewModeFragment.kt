package com.iamport.sampleapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iamport.sampleapp.MyWebViewChromeClient
import com.iamport.sampleapp.MyWebViewClient
import com.iamport.sampleapp.databinding.WebViewModeFragmentBinding
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.EventObserver
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [MobileWebViewModeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MobileWebViewModeFragment : Fragment() {

    private var binding: WebViewModeFragmentBinding? = null
    private var createdView = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        Iamport.init(this)
        binding = WebViewModeFragmentBinding.inflate(inflater, container, false)
        createdView = true
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        if (!createdView) {
            return
        }

        binding?.webview?.let {

            // 통상적인 경우의 custom webviewClient
            it.webViewClient = MyWebViewClient()
            it.webChromeClient = MyWebViewChromeClient()

            // oreo 미만에서 url 변경만 보고 싶은경우
            Iamport.mobileWebModeShouldOverrideUrlLoading()?.observe(this, EventObserver { uri ->
                Log.i("SAMPLE", "changed url :: $uri")
            })

            // 모바일 웹 단독 모드
//            it.loadUrl(CONST.PAYMENT_MOBILE_WEB_FILE_URL)
            it.loadUrl("https://pay-demo.iamport.kr") // 아임포트 데모 페이지
            Iamport.pluginMobileWebSupporter(it) // 로컬 데모 페이지
            createdView = false
        }

        binding?.normalmodeButton?.setOnClickListener {
            Iamport.close()
            popBackStack()
        }
    }

    fun popBackStack() {
        runCatching {
            (activity as MainActivity).popBackStack()
        }.onFailure {
            Log.e("WebViewMode", "돌아갈 수 없습니다.")
        }
    }
}