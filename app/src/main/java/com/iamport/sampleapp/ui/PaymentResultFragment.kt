package com.iamport.sampleapp.ui

import androidx.core.content.ContextCompat
import com.google.gson.GsonBuilder
import com.iamport.sampleapp.PaymentResultData
import com.iamport.sampleapp.R
import com.iamport.sampleapp.databinding.ResultFragmentBinding
import com.iamport.sdk.data.sdk.IamPortResponse

class PaymentResultFragment : BaseFragment<ResultFragmentBinding>() {

    override val layoutResourceId: Int = R.layout.result_fragment

    override fun initStart() {
        super.onStart()
        val impResponse = PaymentResultData.result
        val resultText = if (isSuccess(impResponse)) "결제성공" else "결제실패"
        val color = if (isSuccess(impResponse)) R.color.md_green_200 else R.color.fighting

        val tv = viewDataBinding.resultMessage

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