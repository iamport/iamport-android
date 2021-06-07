package com.iamport.sampleapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
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
 * Use the [WebViewModeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WebViewModeFragment : Fragment() {

    private var binding: WebViewModeFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Iamport.init(this)
        binding = WebViewModeFragmentBinding.inflate(inflater, container, false)
        binding?.webview?.loadUrl("https://github.com/iamport/iamport-android")
        return binding?.root
    }

    override fun onStart() {
        super.onStart()

        // 웹뷰 모드 enable
        binding?.webviewButton?.setOnClickListener {
            Log.d("WebViewMode", "결제 요청!")

            val request = IamPortRequest(
                pg = PG.kcp.makePgRawName(""),         // PG사
                pay_method = PayMethod.card,                    // 결제수단
                name = "웹뷰모드도 진짜 쉬워요!",                      // 주문명
                merchant_uid = getSampleMerchantUid(),     // 주문번호
                amount = "1000",                                // 결제금액
                buyer_name = "김개발"
            )

            binding?.webview?.let {

                this.activity?.onBackPressedDispatcher?.addCallback(this, backPressCallback)

                Iamport.enableWebViewMode(it)
                Log.d("WebViewMode", "iamport sdk webview mode? ${Iamport.isWebViewMode()}")
                // 아임포트에 결제 요청하기
                Iamport.payment("iamport", request, paymentResultCallback = {
                    // 결제 완료 후 결과 콜백을 토스트 메시지로 보여줌
                    Toast.makeText(this.context, "결제결과 => $it", Toast.LENGTH_LONG).show()
                })
            }
        }

        binding?.mobilewebButton?.setOnClickListener {
            // 모바일 웹 단독 모드
            binding?.webview?.let {
                binding?.webview?.loadUrl(CONST.PAYMENT_MOBILE_WEB_FILE_URL)
                Iamport.pluginMobileWebSupporter(it)
            }
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

    // TODO : 이부분은 알맞게 직접 구현해주셔야 합니다.
    private val backPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            binding?.webview?.run {
                if (canGoBack()) { // webview 백버튼 처리 로직
                    goBack()
                } else {
                    remove()
                    popBackStack()
                }
            } ?: run {
                remove()
                popBackStack()
            }
        }
    }

    private fun getSampleMerchantUid(): String {
        return "wvmode_aos_${Date().time}"
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