package com.nagel.wordnotification.presentation.base

import android.view.View
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {
    protected abstract val viewModel: BaseViewModel

    fun <T> renderResult(
        root: View, result: Result<T>,
        onPending: () -> Unit,
        onError: (Exception) -> Unit,
        onSuccessResult: (T) -> Unit
    ) {
        when(result){
            is SuccessResult -> onSuccessResult(result.data)
            is PendingResult -> onPending()
            is ErrorResult -> onError(result.exception)
        }
    }
}