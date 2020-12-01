package com.iamport.sdk.presentation.viewmodel

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamport.sdk.domain.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {

    private val snackbarMessage = MutableLiveData<Event<Int>>()
    private val snackbarMessageString = MutableLiveData<Event<String>>()
    private val snackbarMessageStringButton = MutableLiveData<Event<Triple<String, String, View.OnClickListener>>>()

    fun showSnackbar(stringResourceId: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            snackbarMessage.value = Event(stringResourceId)
        }
    }

    fun showSnackbar(str: String) {
        viewModelScope.launch(Dispatchers.Main) { snackbarMessageString.value = Event(str) }
    }

    fun showSnackbarBtn(str: String, btnName: String, listener: View.OnClickListener) {
        viewModelScope.launch(Dispatchers.Main) {
            snackbarMessageStringButton.value = Event(Triple(str, btnName, listener))
        }
    }

    fun observeSnackbarMessage(lifeCycleOwner: LifecycleOwner, ob: (Event<Int>) -> Unit) {
        snackbarMessage.observe(lifeCycleOwner, ob)
    }

    fun observeSnackbarMessageStr(lifeCycleOwner: LifecycleOwner, ob: (Event<String>) -> Unit) {
        snackbarMessageString.observe(lifeCycleOwner, ob)
    }

    fun observeSnackbarStrBtn(lifeCycleOwner: LifecycleOwner, ob: (Event<Triple<String, String, View.OnClickListener>>) -> Unit) {
        snackbarMessageStringButton.observe(lifeCycleOwner, ob)
    }
}