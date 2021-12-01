package com.iamport.sdk.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iamport.sdk.domain.repository.StrategyRepository
import com.iamport.sdk.domain.utils.NativeLiveDataEventBus
import com.iamport.sdk.domain.utils.WebViewLiveDataEventBus

class MainViewModelFactory(private val bus: NativeLiveDataEventBus, private val repository: StrategyRepository, private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            MainViewModel(bus, repository, app) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}

class WebViewModelFactory(private val bus: WebViewLiveDataEventBus, private val repository: StrategyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(WebViewModel::class.java)) {
            WebViewModel(bus, repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}