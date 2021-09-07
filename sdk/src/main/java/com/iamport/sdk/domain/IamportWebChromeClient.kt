package com.iamport.sdk.domain

import android.R
import android.app.AlertDialog
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView


open class IamportWebChromeClient : WebChromeClient() {
    override fun onJsConfirm(
        view: WebView,
        url: String,
        message: String,
        result: JsResult
    ): Boolean { // 컨펌 창 뜨려고 할때
        AlertDialog.Builder(view.context)
            .setTitle(url + "에 삽입된 내용") // 컨펌 타이틀
            .setMessage(message) // 컨펌 메시지
            .setPositiveButton( // 확인버튼 눌렀을때
                R.string.ok
            ) { _, _ -> result.confirm() }
            .setNegativeButton( // 취소버튼 눌렀을때
                R.string.cancel
            ) { _, _ -> result.cancel() }
            .create()
            .show()
        return true
    }
}