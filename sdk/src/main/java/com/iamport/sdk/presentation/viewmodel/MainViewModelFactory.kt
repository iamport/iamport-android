package com.iamport.sdk.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iamport.sdk.domain.repository.StrategyRepository
import com.iamport.sdk.domain.utils.NativeLiveDataEventBus

class MainViewModelFactory(private val bus: NativeLiveDataEventBus, private val repository: StrategyRepository, private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            MainViewModel(bus, repository, app) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}