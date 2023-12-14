package com.nagel.wordnotification.presentation.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
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
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        scope: CoroutineScope = viewModelScope,
        block: suspend () -> T,
    ) =
        scope.launch(dispatcher) {
            liveResult.postValue(PendingResult())
            try {
                liveResult.postValue(SuccessResult(block()))
            } catch (e: Exception) {
                e.printStackTrace()
                liveResult.postValue(ErrorResult(e))
            }
        }

}