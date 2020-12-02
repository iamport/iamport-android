package com.iamport.sdk.presentation.activity

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.google.gson.GsonBuilder
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.data.sdk.ProvidePgPkg
import com.iamport.sdk.domain.utils.*
import com.iamport.sdk.presentation.contract.ChaiContract
import com.iamport.sdk.presentation.viewmodel.MainViewModel
import com.iamport.sdk.presentation.viewmodel.MainViewModelFactory
import com.orhanobut.logger.Logger.*
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.get


@KoinApiExtension
internal class IamportSdk(
    val activity: ComponentActivity? = null,
    val fragment: Fragment? = null,
    val webViewLauncher: ActivityResultLauncher<Payment>?,
    val close: LiveData<Unit>
) : KoinComponent {

    private val hostHelper: HostHelper = HostHelper(activity, fragment)

    private val launcherChai: ActivityResultLauncher<Pair<String, String>>? // 차이앱 런처
    private val viewModel: MainViewModel // 요청할 뷰모델

    private val delayRun = DelayRun() // 딜레이 호출
    private var paymentCallBack: ((IamPortResponse?) -> Unit)? = null // 콜백함수
    private var preventBackpress: Boolean = false // 종료버튼 막기

    private val isPolling = MutableLiveData<Event<Boolean>>()

    init {
        viewModel = ViewModelProvider(hostHelper.viewModelStoreOwner, MainViewModelFactory(get(), get())).get(MainViewModel::class.java)

        launcherChai = if (hostHelper.mode == MODE.ACTIVITY) {
            activity?.registerForActivityResult(ChaiContract()) { resultCallback() }
        } else {
            fragment?.registerForActivityResult(ChaiContract()) { resultCallback() }
        }

        clearData()
    }

    private val lifecycleObserver = object : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            d("onStop")
            viewModel.pollingChaiStatus() // 백그라운드 진입시 차이 폴링 시작, (webview 이용시에는 폴링하지 않음)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            d("onDestroy")
            clearData()
            hostHelper.lifecycle.removeObserver(this)
        }
    }

    /**
     * BaseActivity 에서 onCreate 시 호출
     */
    fun initStart(payment: Payment, paymentCallBack: ((IamPortResponse?) -> Unit)?) {
        i("HELLO I'MPORT SDK!")
        viewModel.clearData()

        this.preventBackpress = true
        this.paymentCallBack = paymentCallBack

        hostHelper.lifecycle.addObserver(lifecycleObserver)
        observeViewModel(payment) // 관찰할 LiveData
        // 외부에서 종료
        close.observe(hostHelper.lifecycleOwner, Observer { clearData() })

    }

    /**
     * 관찰할 LiveData 옵저빙
     */
    private fun observeViewModel(payment: Payment?) {
        d(GsonBuilder().setPrettyPrinting().create().toJson(payment))
        payment?.let { pay: Payment ->

            // 결제결과 옵저빙
            viewModel.impResponse().observe(hostHelper.lifecycleOwner, EventObserver(this::sdkFinish))

            // 웹뷰앱 열기
            viewModel.webViewPayment().observe(hostHelper.lifecycleOwner, EventObserver(this::requestWebViewPayment))

            // 차이앱 열기
            viewModel.chaiUri().observe(hostHelper.lifecycleOwner, EventObserver(this::openChaiApp))

            // 차이폴링여부
            viewModel.isPolling().observe(hostHelper.lifecycleOwner, EventObserver(this::updatePolling))

            // 결제 시작
            delayRun.launch { requestPayment(pay) }
        }
    }

    fun isPolling(): LiveData<Event<Boolean>> {
        return isPolling
    }

    private fun updatePolling(it: Boolean) {
        isPolling.value = Event(it)
    }


    // 차이 앱 종료 콜백 감지
    private fun resultCallback() {
        i("Result Callback ChaiLauncher")
        preventBackpress = false
        viewModel.checkChaiStatus()
    }

    /**
     * 결제 요청 실행
     */
    private fun requestPayment(it: Payment) {
        // 네트워크 연결 상태 체크
        if (!Util.isInternetAvailable(hostHelper.context)) {
            sdkFinish(Util.getFailResponse(it, "네트워크 연결 안됨"))
            return
        }
        viewModel.judgePayment(it) // 뷰모델에 데이터 판단 요청(native or webview pg)
    }


    /**
     * 웹뷰 결제 요청 실행
     */
    private fun requestWebViewPayment(it: Payment) {
        clearData()
        webViewLauncher?.launch(Payment(it.userCode, it.iamPortRequest))
    }


    /**
     * 뷰모델 데이터 클리어
     */
    fun clearData() {
        d("clearData!")
        preventBackpress = false
        viewModel.clearData()
    }


    /**
     * 모든 결과 처리 및 SDK 종료
     */
    private fun sdkFinish(iamPortResponse: IamPortResponse?) {
        i("명시적 sdkFinish ${iamPortResponse.toString()}")
        clearData()
        paymentCallBack?.invoke(iamPortResponse)
    }


    /**
     * 차이앱 외부앱 열기
     */
    private fun openChaiApp(it: String) {
        i(it)
        runCatching {
            preventBackpress = true
            launcherChai?.launch(it to "openchai")
        }.onFailure { thr: Throwable ->
            i("${thr.message}")
            movePlayStore(Intent.parseUri(it, Intent.URI_INTENT_SCHEME))
            clearData()
        }
    }


    /**
     * 앱 패키지 검색하여 플레이 스토어로 이동
     */
    private fun movePlayStore(intent: Intent) {
        val pkg = intent.`package` ?: run {
            // intent 에 패키지 없으면 ProvidePgPkg에서 intnet.schme 으로 앱 패키지 검색
            i("Not found in intent package")
            when (val providePgPkg = intent.scheme?.let { ProvidePgPkg.from(it) }) {
                null -> {
                    i("Not found in intent schme :: ${intent.scheme}")
                    return@run null
                }
                else -> providePgPkg.pkg
            }
        }

        if (!pkg.isNullOrBlank()) {
            i("movePlayStore :: $pkg")
            Intent(Intent.ACTION_VIEW, Uri.parse(Util.getMarketId(pkg))).let {
                if (hostHelper.mode == MODE.ACTIVITY) {
                    activity?.startActivity(it)
                } else {
                    fragment?.startActivity(it)
                }
            }
        }
    }
}
