package com.iamport.sdk.presentation.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.presentation.activity.WebViewActivity

/**
 * WebView 앱 요청 및 응답 데이터 규약
 */
class WebViewActivityContract : ActivityResultContract<Payment, IamPortResponse>() {

    override fun createIntent(context: Context, input: Payment): Intent {
        return Intent(context, WebViewActivity::class.java).apply {
            // 액티비티 하나 제한
            flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(CONST.CONTRACT_INPUT, Bundle().apply { putParcelable(CONST.BUNDLE_PAYMENT, input) })
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): IamPortResponse? {
        return when (resultCode) {
            Activity.RESULT_OK -> intent?.getParcelableExtra(CONST.CONTRACT_OUTPUT)
            else -> null
        }
    }
}