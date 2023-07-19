package com.iamport.sampleapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.GsonBuilder
import com.iamport.sampleapp.PaymentResultData
import com.iamport.sampleapp.R
import com.iamport.sampleapp.databinding.ResultFragmentBinding
import com.iamport.sdk.data.sdk.IamPortResponse

class PaymentResultFragment : Fragment() {

    private lateinit var binding: ResultFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ResultFragmentBinding.inflate(inflater, container, false)
        initStart()
        return binding.root
    }

    private fun initStart() {
        super.onStart()
        val impResponse = PaymentResultData.result
        val resultText = if (isSuccess(impResponse)) "결제성공" else "결제실패"
        val color = if (isSuccess(impResponse)) R.color.md_green_200 else R.color.fighting

        val tv = binding.resultMessage

        tv.setTextColor(ContextCompat.getColor(requireContext(), color))
        tv.text = "$resultText\n${GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(impResponse)}"
    }

    private fun isSuccess(iamPortResponse: IamPortResponse?): Boolean {
        if (iamPortResponse == null) {
            return false
        }
        return iamPortResponse.success == true || iamPortResponse.imp_success == true
    }
}