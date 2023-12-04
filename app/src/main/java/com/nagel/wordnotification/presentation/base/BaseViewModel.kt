package com.nagel.wordnotification.presentation.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

typealias LiveResult<T> = LiveData<Result<T>>
typealias MutableLiveResult<T> = MutableLiveData<Result<T>>

open class BaseViewModel : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler() { _, ex ->
        ex.printStackTrace()
    }

    open fun onResult(result: Any) {

    }

    fun <T> into(
        liveResult: MutableLiveResult<T>,
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        block: suspend () -> T
    ) =
        viewModelScope.launch(dispatcher + coroutineExceptionHandler) {
            liveResult.postValue(PendingResult())
            try {
                liveResult.postValue(SuccessResult(block()))
            } catch (e: Exception) {
                liveResult.postValue(ErrorResult(e))
            }
        }

}