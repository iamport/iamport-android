package com.iamport.sampleapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.iamport.sampleapp.R;
import com.iamport.sdk.data.sdk.IamPortRequest;
import com.iamport.sdk.data.sdk.PG;
import com.iamport.sdk.data.sdk.PayMethod;
import com.iamport.sdk.domain.core.Iamport;

import java.util.Date;

import kotlin.Unit;


/**
 * JAVA 사용자를 위한 간단 사용예제 입니다.
 * 동작을 보시려면 AndroidManifest.xml 의 activity name 을
 * android:name=".ui.MainActivity_JAVA" 으로 수정 후 빌드해주세요.
 */
public class MainActivity_JAVA extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Iamport.INSTANCE.init(this);

        IamPortRequest request = IamPortRequest.builder()
                .pg(PG.kcp.makePgRawName(""))
                .pay_method(PayMethod.card.name())
                .name("JAVA칩 프라푸치노 주문이요")
                .merchant_uid("mid_" + (new Date()).getTime())
                .amount("3200")
                .buyer_name("김아임포트").build();

        Iamport.INSTANCE.payment("iamport", null, null, request,
                iamPortApprove -> {
                    // (Optional) CHAI 최종 결제전 콜백 함수.
                    return Unit.INSTANCE;
                }, iamPortResponse -> {
                    // 최종 결제결과 콜백 함수.
                    String responseText = iamPortResponse.toString();
                    Log.d("IAMPORT_SAMPLE", responseText);
                    Toast.makeText(this, responseText, Toast.LENGTH_LONG).show();
                    return Unit.INSTANCE;
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Iamport.INSTANCE.close();
    }
}