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
import com.google.gson.GsonBuilder
import com.iamport.sampleapp.PaymentResultData
import com.iamport.sampleapp.ViewModel
import com.iamport.sampleapp.databinding.WebViewModeFragmentBinding
import com.iamport.sdk.data.sdk.IamPortRequest
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.PayMethod
import com.iamport.sdk.domain.core.ICallbackPaymentResult
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
    val viewModel: ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Iamport.init(this)
        binding = WebViewModeFragmentBinding.inflate(inflater, container, false)
//        binding?.webview?.loadUrl("https://github.com/iamport/iamport-android")
        return binding?.root
    }

    override fun onStart() {
        super.onStart()

        // 웹뷰 모드 enable
        Log.d("WebViewMode", "결제 요청!")

        val userCode = viewModel.userCode
        val request = viewModel.createIamPortRequest()

        binding?.webview?.let {

            this.activity?.onBackPressedDispatcher?.addCallback(this, backPressCallback)

            Log.d("WebViewMode", "iamport sdk webview mode? ${Iamport.isWebViewMode()}")
            // 아임포트에 결제 요청하기
            Iamport.payment(userCode, webviewMode = it, iamPortRequest = request, paymentResultCallback = { it ->
                // 결제 완료 후 결과 콜백을 토스트 메시지로 보여줌
//                Toast.makeText(this.context, "결제결과 => $it", Toast.LENGTH_LONG).show()
                callBackListener.result(it)
            })
        }

        binding?.normalmodeButton?.setOnClickListener {
            Iamport.close()
            popBackStack()
        }
    }


    private val callBackListener = object : ICallbackPaymentResult {
        override fun result(iamPortResponse: IamPortResponse?) {
            val resJson = GsonBuilder().setPrettyPrinting().create().toJson(iamPortResponse)
            Log.i("SAMPLE", "결제 결과 콜백\n$resJson")
            PaymentResultData.result = iamPortResponse

            popBackStack()
            if (iamPortResponse != null) {
                (activity as MainActivity).replaceFragment(PaymentResultFragment())
            }
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