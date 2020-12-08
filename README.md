# I'mport Android SDK


## 설명
[아임포트][1]

[아임포트 블로그][2]

[아임포트 docs][3]

[1]: https://www.iamport.kr/
[2]: http://blog.iamport.kr/
[3]: https://docs.iamport.kr/?lang=ko

## 사용방법

### implementation
```gradle
// project gralde
maven {
  url 'https://jitpack.io'
}
```
```gradle
// app gradle
implementation 'com.github.iamport:iamport-android:x.y.z'
```


### KOTLIN usage
```kotlin

// SDK 초기화
// (activity) LifecycleOwners must call register before they are STARTED.
// (fragement) Fragments must call registerForActivityResult() before they are created (i.e. initialization, onAttach(), or onCreate())
fun onCreate() {
    ..
    Iamport.init(this)
    ..
}

// SDK 종료
fun onDestroy() {
..
  Iamport.close() // 화면을 나가는 시점, 화면이 꺼지는 시점(onDestroy, onDetach 등)에 추가
..
}

/**
 * SDK 에 결제 요청할 데이터 구성
 */
val request = IamPortRequest(
    pg = "chai",                                 // PG 사
    pay_method = PayMethod.trans,                 // 결제수단
    name = "여기주문이요",                           // 주문명
    merchant_uid = "mid_123456",                 // 주문번호
    amount = "3000",                             // 결제금액
    buyer_name = "홍길동"
)

// 결제요청
Iamport.payment("imp123456", request,
    approveCallback = { /* (Optional) 차이 최종 결제전 콜백 함수. */ },
    paymentResultCallback = { /* 최종 결제 후 콜백함수 */ })

```

> (Optional) 차이결제 + approveCallback 가 있을 때, 

> 콜백 전달 받은 후(타임아웃 : CONST.CHAI_FINAL_PAYMENT_TIME_OUT_SEC)에 아래 함수 호출
```kotlin
Iamport.chaiPayment(iamPortApprove) // 재고 등 확인 후, 차이 최종 결제 요청 실행.
```

> (Optional) 차이폴링 여부 확인
```kotlin
// 차이 결제 상태체크 폴링 여부를 확인하실 수 있습니다.
Iamport.isPolling()?.observe(this, EventObserver {
    i("차이 폴링? :: $it")
})

// 또는, 폴링 상태를 보고 싶을때 명시적으로 호출
i("isPolling? ${Iamport.isPolling()?.value?.peekContent()}")
```

---

<details>
<summary><strong>JAVA usage 펼쳐보기</strong></summary>

### java usage
```java

public void onCreate() {
  Iamport.INSTANCE.init(this);
}

public void onDeatroy() {
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

> (Optional) 차이결제 + approveCallback 가 있을 때, 

> 콜백 전달 받은 후(타임아웃 : CONST.CHAI_FINAL_PAYMENT_TIME_OUT_SEC)에 아래 함수 호출
```java
Iamport.INSTANCE.chaiPayment(iamPortApprove) // 재고 등 확인 후, 차이 최종 결제 요청 실행.
```

> 자바 프로젝트에선 kotin stblib 추가가 필요합니다
[$코틀린_버전][4]

```gradle 
implementation "org.jetbrains.kotlin:kotlin-stdlib:$코틀린_버전"
```

[4]: https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib

</details>

---

## 샘플앱
[앱 소스 경로](../iamport-android/app)

[PaymentFragment](../iamport-android/app/src/main/java/com/iamport/sampleapp/ui/PaymentFragment.kt)

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
        
        Iamport.payment(userCode, request, approveCallback = { approveCallback(it) }, paymentResultCallback = { callBackListener.result(it) })
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
```




