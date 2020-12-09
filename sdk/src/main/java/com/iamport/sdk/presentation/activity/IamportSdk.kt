package com.iamport.sdk.presentation.activity

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.google.gson.GsonBuilder
import com.iamport.sdk.data.sdk.IamPortApprove
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.data.sdk.ProvidePgPkg
import com.iamport.sdk.domain.utils.*
import com.iamport.sdk.domain.utils.Util.observeAlways
import com.iamport.sdk.presentation.contract.ChaiContract
import com.iamport.sdk.presentation.viewmodel.MainViewModel
import com.iamport.sdk.presentation.viewmodel.MainViewModelFactory
import com.orhanobut.logger.Logger.*
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.*


@KoinApiExtension
internal class IamportSdk(
    val activity: ComponentActivity? = null,
    val fragment: Fragment? = null,
    val webViewLauncher: ActivityResultLauncher<Payment>?,
    val approvePayment: LiveData<Event<IamPortApprove>>,
    val close: LiveData<Event<Unit>>,
) : KoinComponent {

    private val hostHelper: HostHelper = HostHelper(activity, fragment)

    private val launcherChai: ActivityResultLauncher<Pair<String, String>>? // 차이앱 런처
    private val viewModel: MainViewModel // 요청할 뷰모델

    private var paymentResultCallBack: ((IamPortResponse?) -> Unit)? = null // 콜백함수
    private var chaiApproveCallBack: ((IamPortApprove) -> Unit)? = null // 콜백함수
    private val isPolling = MutableLiveData<Event<Boolean>>()

    private val delayRun = DelayRun() // 딜레이 호출
    private var preventBackpress: Boolean = false // 종료버튼 막기


    init {
        viewModel = ViewModelProvider(hostHelper.viewModelStoreOwner, MainViewModelFactory(get(), get())).get(MainViewModel::class.java)

        launcherChai = if (hostHelper.mode == MODE.ACTIVITY) {
            activity?.registerForActivityResult(ChaiContract()) { resultCallback() }
        } else {
            fragment?.registerForActivityResult(ChaiContract()) { resultCallback() }
        }
        clearData()
//        repeatTopPackage()
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
    fun initStart(payment: Payment, approveCallback: ((IamPortApprove) -> Unit)?, paymentResultCallBack: ((IamPortResponse?) -> Unit)?) {
        i("HELLO I'MPORT SDK! ${Util.versionName(hostHelper)}")
        viewModel.clearData()

        this.preventBackpress = true
        this.chaiApproveCallBack = approveCallback
        this.paymentResultCallBack = paymentResultCallBack

        hostHelper.lifecycle.addObserver(lifecycleObserver)
        observeViewModel(payment) // 관찰할 LiveData

        // 차이 최종결제 요청
        approvePayment.observeAlways(hostHelper.lifecycleOwner, EventObserver { viewModel.requestApprovePayments(it) })

        // 외부에서 종료
        close.observeAlways(hostHelper.lifecycleOwner, EventObserver { clearData() })
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

            // 차이 결제 상태 approve 처리
            viewModel.chaiApprove().observeAlways(hostHelper.lifecycleOwner, EventObserver(this::chaiApprove))

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

    private fun chaiApprove(approve: IamPortApprove) {
        chaiApproveCallBack?.run {
            invoke(approve)
        } ?: run {
            viewModel.requestApprovePayments(approve)
        }
    }

    /**
     * 차이 앱 종료 콜백 감지
     */
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
            sdkFinish(IamPortResponse.makeFail(it, msg = "네트워크 연결 안됨"))
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

//    fun onUserLeaveHint() {
//        hostHelper.activity = object : Activity {
//            override fun onUserLeaveHint() {
//                super.onUserLeaveHint()
//            }
//        }
//    }


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
        paymentResultCallBack?.invoke(iamPortResponse)
    }


    /**
     * 차이앱 외부앱 열기
     */
    private fun openChaiApp(it: String) {
        i("openChaiApp")
        d(it)
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

//    private fun repeatTopPackage() {
//        viewModel.viewModelScope.launch(Dispatchers.Default) {
//            repeat(10000) {
//                delay(2000)
//                checkingTopPackage()
//            }
//        }
//    }

//    private fun checkingTopPackage() {
//        val am = hostHelper.context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            for ( task in am.appTasks) {
//                i("top activity ::: ${task.taskInfo.topActivity?.className}")
//                i("top packageName ::: ${task.taskInfo.topActivity?.packageName}")
//            }
//
//            val tasks = am.getRunningTasks(1)
//            i("RunningTasks packageName ::: ${tasks[0].topActivity?.packageName}")
//        }
//    }

}
