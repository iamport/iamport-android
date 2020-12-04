package com.iamport.sampleapp.ui

import android.app.AlertDialog.Builder
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import com.google.gson.GsonBuilder
import com.iamport.sampleapp.PaymentResultData.result
import com.iamport.sampleapp.R
import com.iamport.sampleapp.databinding.PaymentFragmentBinding
import com.iamport.sdk.data.sdk.*
import com.iamport.sdk.domain.sdk.ICallbackPaymentResult
import com.iamport.sdk.domain.sdk.Iamport
import com.iamport.sdk.domain.utils.EventObserver
import com.iamport.sdk.domain.utils.Util
import com.orhanobut.logger.Logger.d
import com.orhanobut.logger.Logger.i
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class PaymentFragment : BaseFragment<PaymentFragmentBinding>() {
    override val layoutResourceId: Int = R.layout.payment_fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // onCreate 시 필수적으로 init 을 해주셔야 합니다.
        Iamport.init(this) // fragment
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressCallback)
    }

    override fun onDetach() {
        super.onDetach()
        Iamport.close()
        backPressCallback.remove()
    }

    override fun initStart() {

        viewDataBinding.paymentButton.setOnClickListener {
            onClickPayment()
        }

        viewDataBinding.backButton.setOnClickListener {
            backPressCallback.handleOnBackPressed()
        }

        val userCodeAdapter = ArrayAdapter(
            requireContext(), R.layout.support_simple_spinner_dropdown_item,
            Util.DevUserCode.getUserCodes()
        )

        val pgAdapter = ArrayAdapter(
            requireContext(), R.layout.support_simple_spinner_dropdown_item,
            PG.getPGNames()
        )

        viewDataBinding.userCode.adapter = userCodeAdapter

        viewDataBinding.pg.adapter = pgAdapter
        viewDataBinding.pg.onItemSelectedListener = pgSelectListener

        viewDataBinding.name.setText("아임포트 Android SDK 결제 테스트")
        viewDataBinding.amount.setText("1000")
    }

    override fun onStart() {
        super.onStart()
        viewDataBinding.merchantUid.setText(getRandomMerchantUid())
        onPolling()
    }


    private fun onPolling() {
        // 차이 결제 상태체크 폴링 여부를 확인하실 수 있습니다.
        Iamport.isPolling()?.observe(this, EventObserver {
            i("차이 폴링? :: $it")
        })

        // 또는, 폴링 상태를 보고 싶을때 명시적으로 호출
        i("${Iamport.isPolling()?.value?.peekContent()}")
    }

    // 결제 버튼 클릭
    private fun onClickPayment() {

        val pg = PG.values()[viewDataBinding.pg.selectedItemPosition]
        val payMethod = PayMethod.values()[viewDataBinding.pgMethod.selectedItemPosition]

        val paymentName = viewDataBinding.name.text.toString().trim()
        val merchantUid = viewDataBinding.merchantUid.text.toString().trim()
        val amount = viewDataBinding.amount.text.toString().trim()

        /**
         * SDK 에 결제 요청할 데이터 구성
         */
        val request = IamPortRequest(
            pg = pg.getPgSting(storeId = ""),           // PG 사
            pay_method = payMethod,                     // 결제수단
            name = paymentName,                         // 주문명
            merchant_uid = merchantUid,                 // 주문번호
            amount = amount,                            // 결제금액
            buyer_name = "남궁안녕"
        )

        i(GsonBuilder().setPrettyPrinting().create().toJson(request))

        val userCode = Util.DevUserCode.values()[viewDataBinding.userCode.selectedItemPosition].name

        /**
         * 결제요청 Type#1 ICallbackPaymentResult 구현을 통한 결제결과 callback
         */
//        Iamport.payment(userCode, request, callback = callBackListener)

        /**
         * 결제요청 Type#2 함수 호출을 통한 결제결과 callbck
         */
//        Iamport.payment(userCode, request) { callBackListener.result(it) }
        Iamport.payment(userCode, request, approveCallback = { approveCallback(it) }, callback = { callBackListener.result(it) })
    }

    // TODO 재고확인 등 최종결제를 위한 처리를 해주세요
    private fun approveCallback(iamPortApprove: IamPortApprove) {
        val secUnit = 1000L
        val sec = 1
        GlobalScope.launch {
            i("재고확인 합니다~~")
            delay(sec * secUnit) // sec 초간 재고확인 프로세스를 가정합니다
            Iamport.chaiPayment(iamPortApprove) // TODO: 12/4/20 상태 확인 후 SDK 에 최종결제 요청
        }
    }

    private val callBackListener = object : ICallbackPaymentResult {
        override fun result(iamPortResponse: IamPortResponse?) {
            val resJson = GsonBuilder().setPrettyPrinting().create().toJson(iamPortResponse)
            i("머천트 앱 결과 뿅\n$resJson")
            result = iamPortResponse
            if (iamPortResponse != null) {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.container, PaymentResultFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private val pgSelectListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            viewDataBinding.pgMethod.adapter = ArrayAdapter(
                requireContext(), R.layout.support_simple_spinner_dropdown_item,
                Util.convertPayMethodNames(PG.values()[position])
            )
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }

    private val backPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

            Builder(view?.context)
                .setTitle("결제를 종료하시겠습니까?") // 컨펌 타이틀
                .setMessage("확인시 앱 종료") // 컨펌 메시지
                // 확인버튼 눌렀을때 동작
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    Iamport.close() // 명시적인 SDK 종료
                    requireActivity().finish()
                }
                // 취소버튼 눌렀을때 동작
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    i("닫기")
                }
                .create()
                .show()
        }
    }

    private fun getRandomMerchantUid(): String {
        return "muid_aos_${Date().time}"
    }

}
