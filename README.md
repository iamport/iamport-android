# :seedling: I'mport Android SDK :seedling:

[![](https://jitpack.io/v/iamport/iamport-android.svg)](https://jitpack.io/#iamport/iamport-android)

## 설명

안드로이드 네이티브 앱에서 결제 개발을 간편하게 도와주는 아임포트 SDK 입니다.

- CHAI 간편결제는 Native 연동되어 있습니다.

- 그외 PG 들은 WebView 기반으로 연동되어 있습니다.

- 추후 순차적으로 타 간편결제들도 네이티브 연동 예정입니다. 

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

  // 일반적인 경우
  // 사용하시는 안드로이드 어플리케이션 클래스에 추가하세요
  class BaseApplication : Application() {
    override fun onCreate() {
        ..
        Iamport.create(this)
    }
  }
    
   // DI 로 koin 을 사용하시는 경우 
   // 생성된 koinApplication 을 파라미터로 넘겨주셔야 합니다
   class BaseApplication : Application() {
    override fun onCreate() {
        ..   
        val koinApp = startKoin { .. }
        Iamport.createWithKoin(this, koinApp)
    }
    
    // KoinApplication 이 필요한 경우
    Iamport.getKoinApplition() 
}

```
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
      approveCallback = { /* (Optional) CHAI 최종 결제전 콜백 함수. */ },
      paymentResultCallback = { /* 최종 결제결과 콜백 함수. */ })
      

```


### Optional 구현사항 for CHAI 결제
> - 차이 결제에서 approveCallback 이 있을 때 (최종 결제전 재고 확인 등이 필요할 때)  
콜백 전달 받은 후에 chaiPayment 함수 호출  
(타임아웃 : CONST.CHAI_FINAL_PAYMENT_TIME_OUT_SEC)
```kotlin
  Iamport.chaiPayment(iamPortApprove) // 재고 등 확인 후, 차이 최종 결제 요청 실행.
```


> - 차이 결제 폴링 여부 확인
```kotlin
  // 차이 결제 상태체크 폴링 여부를 확인하실 수 있습니다.
  Iamport.isPolling()?.observe(this, EventObserver {
      i("차이 폴링? :: $it")
  })

  // 또는, 폴링 상태를 보고 싶을때 명시적으로 호출
  i("isPolling? ${Iamport.isPollingValue()}")
```



> - 차이 결제 폴링 중에는 포그라운드 서비스가 알람에 뜨게 됩니다.  
enableService = true 라면, 폴링중 포그라운드 서비스를 보여줍니다.  
enableFailStopButton = true 라면, 포그라운드 서비스에서 중지 버튼 생성합니다.  
(해당 enableChaiPollingForegroundService(false, false) 를 Iamport.payment(결제 함수) 전에 호출해주시면 포그라운드 서비스를 등록하지 않습니다)

```kotlin
  Iamport.enableChaiPollingForegroundService(enableService = true, enableFailStopButton = true)
```


> - 포그라운드 서비스 알람 및 중지 버튼 클릭시 동작을   
아래 값의 브로드 캐스트 리시버를 통해 캐치할 수 있습니다.

[샘플앱의 예시 MerchantReceiver.kt](./app/src/main/java/com/iamport/sampleapp/MerchantReceiver.kt)

```kotlin
  const val BROADCAST_FOREGROUND_SERVICE = "com.iamport.sdk.broadcast.fgservice"
  const val BROADCAST_FOREGROUND_SERVICE_STOP = "com.iamport.sdk.broadcast.fgservice.stop"
```

- (포그라운드 서비스 직접 구현시에는 enableService = false 로 설정하고,  
Iamport.isPolling()?.observe 에서 true 전달 받을 시점에, 직접 포그라운드 서비스 만들어 띄우시면 됩니다.)

---

## 자바 프로젝트는 아래 [펼쳐보기] 를 참조해주세요
<details>
<summary>펼쳐보기</summary>

### JAVA usage

> 자바 프로젝트에선 app build.gradle 에서 kotin-stblib 추가가 필요합니다
[$코틀린-버전][4]

```gradle 
  implementation "org.jetbrains.kotlin:kotlin-stdlib:$코틀린-버전"
```

> 필수구현 사항. SDK 제공 api 별 설명은 위의 [KOTLIN usage][6] 를 참고하세요.

[6]:https://github.com/iamport/iamport-android#kotlin-usage

```java
  // 일반적인 경우
  // 사용하시는 안드로이드 어플리케이션 클래스에 추가하세요
  public class BaseApplication extends Application {
      @Override
      public void onCreate() {
          ..
          Iamport.INSTANCE.create(this, null);
      }
  }

   // DI 로 koin 을 사용하시는 경우 
   // 생성된 koinApplication 을 파라미터로 넘겨주셔야 합니다
    public class BaseApplication extends Application {
        @Override
        public void onCreate() {
            ..
            KoinApplication koinApp = ..
            Iamport.INSTANCE.createWithKoin(this, koinApp);
        }
    }

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
      // (Optional) CHAI 최종 결제전 콜백 함수.
      return Unit.INSTANCE;
  }, iamPortResponse -> {
      // 최종 결제결과 콜백 함수.
      return Unit.INSTANCE;
  });
```


### Optional 구현사항 for CHAI 결제
> - 차이 결제에서 approveCallback 이 있을 때 (최종 결제전 재고 확인 등이 필요할 때)
```java
  Iamport.INSTANCE.chaiPayment(iamPortApprove) // 재고 등 확인 후, 차이 최종 결제 요청 실행.
```

[4]: https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib


> - 차이 결제 폴링 여부 확인
```java
  Iamport.INSTANCE.isPolling().observe(this, EventObserver -> {
      i("차이 폴링? :: " + it)
  });

  i("isPolling? " + Iamport.INSTANCE.isPollingValue())
```


> - 차이 결제 폴링 중에는 포그라운드 서비스가 알람에 뜨게 됩니다.  
```java
  Iamport.INSTANCE.enableChaiPollingForegroundService(true, true)
```


> - 포그라운드 서비스 알람 및 중지 버튼 클릭시 동작을 아래 값의 브로드 캐스트 리시버를 통해 캐치할 수 있습니다.
```kotlin
  const val BROADCAST_FOREGROUND_SERVICE = "com.iamport.sdk.broadcast.fgservice"
  const val BROADCAST_FOREGROUND_SERVICE_STOP = "com.iamport.sdk.broadcast.fgservice.stop"
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

[BaseApplication.kt (SDK 생성)](./app/src/main/java/com/iamport/sampleapp/BaseApplication.kt)

```kotlin
    override fun onCreate() {
        super.onCreate()
        Iamport.create(this)

        /**
         * DI 로 KOIN 사용시 아래와 같이 사용
        val koinApp = startKoin {
            logger(AndroidLogger())
            androidContext(this@BaseApplication)
        }
        Iamport.create(this, koinApp)
         */
    }
```

[PaymentFragment.kt (결제 화면)](./app/src/main/java/com/iamport/sampleapp/ui/PaymentFragment.kt)

```kotlin

    // 초기화 처리
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Iamport.init(this) // fragment
    }


    // 포그라운드 서비스 처리용 브로드 캐스트 리시버 등록
    override fun onAttach(context: Context) {
        super.onAttach(context)
        registForegroundServiceReceiver(context)
        ..
    }

 
    // 종료 처리
    override fun onDestroy() {
        super.onDestroy()
        Iamport.close()
        ..
    }
    
    
    // 결제버튼 클릭
    private fun onClickPayment() {
        ..
        val request = IamPortRequest(
            pg = pg.getPgSting(pgId = ""),              // PG 사
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
