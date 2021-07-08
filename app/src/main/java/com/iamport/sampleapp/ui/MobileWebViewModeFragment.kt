package com.iamport.sampleapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.iamport.sampleapp.ViewModel
import com.iamport.sampleapp.databinding.WebViewModeFragmentBinding
import com.iamport.sdk.data.sdk.IamPortRequest
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.PayMethod
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.Util
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
        Iamport.init(this)
        binding = WebViewModeFragmentBinding.inflate(inflater, container, false)
        createdView = true
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        if(!createdView) {
            return
        }

        // 모바일 웹 단독 모드
        binding?.webview?.let {
                it.loadUrl(CONST.PAYMENT_MOBILE_WEB_FILE_URL)
//            it.loadUrl("https://www.iamport.kr/demo") // 아임포트 데모 페이지
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.webview?.removeAllViews()
        binding = null
    }

    override fun onDestroy() {
        binding?.webview?.destroy()
        Iamport.close()
        super.onDestroy()
    }
}