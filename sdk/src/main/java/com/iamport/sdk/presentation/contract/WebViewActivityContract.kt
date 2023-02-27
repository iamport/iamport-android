package com.iamport.sdk.presentation.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.iamport.sdk.data.sdk.IamportResponse
import com.iamport.sdk.data.sdk.IamportRequest
import com.iamport.sdk.domain.utils.Constant
import com.iamport.sdk.presentation.activity.WebViewActivity
import com.orhanobut.logger.Logger

/**
 * WebView 앱 요청 및 응답 데이터 규약
 */
class WebViewActivityContract : ActivityResultContract<IamportRequest, IamportResponse?>() {

    override fun createIntent(context: Context, input: IamportRequest): Intent {
        return Intent(context, WebViewActivity::class.java).apply {
            // 액티비티 하나 제한
            flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(Constant.CONTRACT_INPUT, Bundle().apply { putParcelable(Constant.BUNDLE_PAYMENT, input) })
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): IamportResponse? {
        return when (resultCode) {
            Activity.RESULT_OK -> intent?.getParcelableExtra(Constant.CONTRACT_OUTPUT)
            else -> {
                Logger.w("WebViewActivityContract RESULT IS NOT OK :: ${resultCode}")
                null
            }
        }
    }
}