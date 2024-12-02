package com.nagel.wordnotification.presentation.exportdictionaries

import com.nagel.wordnotification.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExportVM @Inject constructor(
) : BaseViewModel() {

    var isStarted: Boolean = false

}