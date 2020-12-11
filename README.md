# :seedling: I'mport Android SDK :seedling:


## 설명

안드로이드 네이티브 앱에서 결제 개발을 간편하게 도와주는 아임포트 SDK 입니다.

- CHAI 간편결제는 Native 연동되어 있으며 (현재 staging 서버이므로 테스트만 가능)

- 그외 PG 들은 WebView 기반으로 연동되어 있고 (실제 결제 가능) 

- 추후 네이티브 순차적으로 연동 예정입니다. 

--- 

- [아임포트][1]

- [아임포트 블로그][2]

- [아임포트 docs][3]

[1]: https://www.iamport.kr/
[2]: http://blog.iamport.kr/
[3]: https://docs.iamport.kr/?lang=ko

## :fire: 사용방법

### Gradle implementation
> project build.gradle
```gradle
  maven {
    url 'https://jitpack.io'
  }
```

> app build.gradle 
[$SDK-VERSION][5]
```gradle
  implementation 'com.github.iamport:iamport-android:$SDK-VERSION'
```

[5]: https://github.com/iamport/iamport-android/releases


### KOTLIN usage

> 필수구현 사항
```kotlin

  // SDK 초기화
  // activity 에서 호출시 : LifecycleOwners must call register before they are STARTED.
  // fragement 에서 호출시 : Fragments must call before they are created (i.e. initialization, onAttach(), or onCreate())
  fun onCreate() {
      Iamport.init(this)
      ..
  }

  // SDK 종료
  // activity 에서 호출시 : onDestroy
  // fragement 에서 호출시 : onDestroy, onDetach 등
  // 공통 : 화면을 나가는 시점, 꺼지는 시점 등에 추가
  fun onDestroy() {
    Iamport.close() 
    ..
  }


  // SDK 에 결제 요청할 데이터 구성
  val request = IamPortRequest(
      pg = "chai",                                   // PG 사
      pay_method = PayMethod.trans,                 // 결제수단
      name = "여기주문이요",                          // 주문명
      merchant_uid = "mid_123456",                // 주문번호
      amount = "3000",                           // 결제금액
      buyer_name = "홍길동"
  )

  // 결제요청
  Iamport.payment("imp123456", request,
      approveCallback = { /* (Optional) 차이 최종 결제전 콜백 함수. */ },
      paymentResultCallback = { /* 최종 결제 후 콜백함수 */ })
      

```

> (Optional) 차이 결제에서 approveCallback 이 있을 때 (최종 결제전 재고 확인 등이 필요할 때)

> 콜백 전달 받은 후에 chaiPayment 함수 호출 
(타임아웃 : CONST.CHAI_FINAL_PAYMENT_TIME_OUT_SEC)
```kotlin
  Iamport.chaiPayment(iamPortApprove) // 재고 등 확인 후, 차이 최종 결제 요청 실행.
```

> (Optional) 차이 결제 폴링 여부 확인
```kotlin
  // 차이 결제 상태체크 폴링 여부를 확인하실 수 있습니다.
  Iamport.isPolling()?.observe(this, EventObserver {
      i("차이 폴링? :: $it")
  })

  // 또는, 폴링 상태를 보고 싶을때 명시적으로 호출
  i("isPolling? ${Iamport.isPollingValue()}")
```


> (Optional) 차이 결제 폴링 중에는 포그라운드 서비스가 알람에 뜨게 됩니다.

> 해당 enableChaiPollingForegroundService(false) 를 Iamport.payment(결제 함수) 전에 호출해주시면 포그라운드 서비스를 등록하지 않습니다
```kotlin
    Iamport.enableChaiPollingForegroundService(false) // default true
```


---

자바 프로젝트는 이쪽을 참조해주세요
<details>
<summary>JAVA usage 펼쳐보기</summary>

### JAVA usage
> 필수구현 사항

> 자바 프로젝트에선 app build.gradle 에서 kotin-stblib 추가가 필요합니다
[$코틀린-버전][4]

```gradle 
  implementation "org.jetbrains.kotlin:kotlin-stdlib:$코틀린-버전"
```

```java

  @Override
  public void onCreate() {
    Iamport.INSTANCE.init(this);
    ..
  }

  @Override
  public void onDeatroy() {
    ..
    Iamport.INSTANCE.close();
  }

  IamPortRequest request
          = IamPortRequest.builder()
          .pg("chai")
          .pay_method(PayMethod.trans)
          .name("여기주문이요")
          .merchant_uid("mid_123456")
          .amount("3000")
          .buyer_name("홍길동").build();

  Iamport.INSTANCE.payment("imp123456", request, 
    iamPortApprove -> {
      // (Optional) 차이 최종 결제전 콜백 함수.
      return Unit.INSTANCE;
  }, iamPortResponse -> {
      // 최종 결제 후 콜백함수
      return Unit.INSTANCE;
  });
```


> (Optional) 차이 결제에서 approveCallback 이 있을 때 (최종 결제전 재고 확인 등이 필요할 때)

> 콜백 전달 받은 후에 chaiPayment 함수 호출 
(타임아웃 : CONST.CHAI_FINAL_PAYMENT_TIME_OUT_SEC)
```java
  Iamport.INSTANCE.chaiPayment(iamPortApprove) // 재고 등 확인 후, 차이 최종 결제 요청 실행.
```

[4]: https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib


> (Optional) 차이폴링 여부 확인
```java
  Iamport.INSTANCE.isPolling().observe(this, EventObserver -> {
      i("차이 폴링? :: " + it)
  });

  i("isPolling? " + Iamport.INSTANCE.isPollingValue())
```

</details>

---

## :bulb: 샘플앱

[앱 소스 확인 경로](./app/src/main/java/com/iamport/sampleapp)

<p float="left">
<img src="./img/chai_sample.webp">
<img src="./img/kcp_sample.webp">
</p>

1. git clone 
2. Android Studio project open
3. build app

---

[MainActivity.kt (Host Activity)](./app/src/main/java/com/iamport/sampleapp/ui/MainActivity.kt)

```kotlin
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        Iamport.catchUserLeave() // TODO SDK 백그라운드 작업 중지를 위해서 onUserLeaveHint 에서 필수 호출!
    }
```

[PaymentFragment.kt (결제 화면)](./app/src/main/java/com/iamport/sampleapp/ui/PaymentFragment.kt)

```kotlin

    // 초기화 처리
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Iamport.init(this) // fragment
    }
    
    // 종료 처리
    override fun onDetach() {
        super.onDetach()
        Iamport.close()
        ..
    }
    
    // 결제버튼 클릭
    private fun onClickPayment() {
        ..
        val request = IamPortRequest(
            pg = pg.getPgSting(storeId = ""),           // PG 사
            pay_method = payMethod,                     // 결제수단
            name = paymentName,                         // 주문명
            merchant_uid = merchantUid,                 // 주문번호
            amount = amount,                            // 결제금액
            buyer_name = "남궁안녕"
        )
        
        // 결제호출
        Iamport.payment(userCode, request,
            approveCallback = { approveCallback(it) },
            paymentResultCallback = { callBackListener.result(it) })
    }
    
    // 차이 결제전 콜백 및 최종 결제 요청 처리
    private fun approveCallback(iamPortApprove: IamPortApprove) {
        val secUnit = 1000L
        val sec = 1
        GlobalScope.launch {
            delay(sec * secUnit) // sec 초간 재고확인 프로세스를 가정합니다
            Iamport.chaiPayment(iamPortApprove) // TODO: 상태 확인 후 SDK 에 최종결제 요청
        }
    }
    
    // fragment 에서 명시적인 종료할 때 처리 Iamport.close()
    private val backPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            Builder(view?.context)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    Iamport.close() // TODO 명시적인 SDK 종료
                    requireActivity().finish()
                }
               ..
        }
    }
```
