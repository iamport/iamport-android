package com.iamport.sampleapp.ui

import android.app.AlertDialog.Builder
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import com.google.gson.GsonBuilder
import com.iamport.sampleapp.MerchantReceiver
import com.iamport.sampleapp.PaymentResultData.result
import com.iamport.sampleapp.R
import com.iamport.sampleapp.databinding.PaymentFragmentBinding
import com.iamport.sdk.data.sdk.*
import com.iamport.sdk.domain.core.ICallbackPaymentResult
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.EventObserver
import com.iamport.sdk.domain.utils.Util
import com.orhanobut.logger.Logger.i
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class PaymentFragment : BaseFragment<PaymentFragmentBinding>() {
    override val layoutResourceId: Int = R.layout.payment_fragment
    private val receiver = MerchantReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        this.context?.unregisterReceiver(receiver)
    }

    // 차이 폴링중에 포그라운드 서비스 생성
    // (* 포그라운드 서비스 직접 구현시에는 enableService = false 로 설정하고,
    // Iamport.isPolling()?.observe 에서 true 전달 받을 시점에, 직접 포그라운드 서비스 만들어 띄우시면 됩니다.)
    private fun registForegroundServiceReceiver() {
        // enableService = true 시, 폴링중 포그라운드 서비스를 보여줍니다.
        // enableFailStopButton = true 시, 포그라운드 서비스에서 중지 버튼 생성합니다.
        Iamport.enableChaiPollingForegroundService(enableService = true, enableFailStopButton = true)

        // 포그라운드 서비스 및 포그라운드 서비스 중지 버튼 클릭시 전달받는 broadcast 리시버
        this.context?.registerReceiver(receiver, IntentFilter().apply {
            addAction(CONST.BROADCAST_FOREGROUND_SERVICE)
            addAction(CONST.BROADCAST_FOREGROUND_SERVICE_STOP)
        })

    }

    override fun initStart() {

        registForegroundServiceReceiver()

        viewDataBinding.paymentButton.setOnClickListener {
            onClickPayment()
        }

        viewDataBinding.backButton.setOnClickListener {
            backPressCallback.handleOnBackPressed()
        }

        val userCodeAdapter = ArrayAdapter(
            requireContext(), R.layout.support_simple_spinner_dropdown_item,
            Util.getUserCodeList()
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
//        onPolling()
    }


    private fun onPolling() {
        // 차이 결제 상태체크 폴링 여부를 확인하실 수 있습니다.
        Iamport.isPolling()?.observe(this, EventObserver {
            i("차이 폴링? :: $it")
        })

        // 또는, 폴링 상태를 보고 싶을 때 명시적으로 호출
        i("isPolling? ${Iamport.isPollingValue()}")
    }


    // 결제 버튼 클릭
    private fun onClickPayment() {

        val pg = PG.values()[viewDataBinding.pg.selectedItemPosition]
        val payMethod = PayMethod.from(viewDataBinding.pgMethod.selectedItem.toString())

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

        val userCode = Util.getUserCode(viewDataBinding.userCode.selectedItemPosition)
        i("userCode :: $userCode")
        i(GsonBuilder().setPrettyPrinting().create().toJson(request))

        /**
         * 결제요청 Type#1 ICallbackPaymentResult 구현을 통한 결제결과 callback
         */
//        Iamport.payment(userCode, request, paymentResultCallback = callBackListener)
//        Iamport.payment(userCode, request, approveCallback = { approveCallback(it) }, paymentResultCallback = callBackListener)

        /**
         * 결제요청 Type#2 함수 호출을 통한 결제결과 callbck
         */
//        Iamport.payment(userCode, request) { callBackListener.result(it) }
        Iamport.payment(userCode, request,
            approveCallback = { approveCallback(it) },
            paymentResultCallback = { callBackListener.result(it) })
    }

    /**
     *  TODO 재고확인 등 최종결제를 위한 처리를 해주세요
     *  CONST.CHAI_FINAL_PAYMENT_TIME_OUT_SEC 만큼 타임아웃 후 결제 데이터가
     *  초기화 되기 때문에 타임아웃 시간 안에 Iamport.chaiPayment 함수를 호출해주셔야 합니다.
     */
    private fun approveCallback(iamPortApprove: IamPortApprove) {
        val secUnit = 1000L
        val sec = 1
        GlobalScope.launch {
            i("재고확인 합니다~~")
            delay(sec * secUnit) // sec 초간 재고확인 프로세스를 가정합니다
            Iamport.chaiPayment(iamPortApprove) // TODO: 상태 확인 후 SDK 에 최종결제 요청
        }
    }

    private val callBackListener = object : ICallbackPaymentResult {
        override fun result(iamPortResponse: IamPortResponse?) {
            val resJson = GsonBuilder().setPrettyPrinting().create().toJson(iamPortResponse)
            i("결제 결과 콜백\n$resJson")
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
