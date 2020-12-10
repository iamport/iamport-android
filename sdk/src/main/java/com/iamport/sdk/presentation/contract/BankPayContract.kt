package com.iamport.sdk.presentation.contract

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.iamport.sdk.data.nice.NiceBankpay
import com.iamport.sdk.data.sdk.ProvidePgPkg

/**
 * 뱅크페이 앱 요청 및 응답 데이터 규약
 */
class BankPayContract : ActivityResultContract<String, Pair<String, String>>() {

    override fun createIntent(context: Context, input: String?): Intent {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.component = ComponentName(ProvidePgPkg.BANKPAY.pkg, ProvidePgPkg.getNiceBankPayAppCls())
        return intent.apply { putExtra(NiceBankpay.INTENT_RESULT_NAME, input) }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Pair<String, String>? {
        return when (resultCode) {
            Activity.RESULT_OK -> {
                val code = intent?.getStringExtra(NiceBankpay.CODE)
                val value = intent?.getStringExtra(NiceBankpay.VALUE)
                if (code != null && value != null) {
                    Pair(code, value)
                } else null
            }
            else -> null
        }
    }
}