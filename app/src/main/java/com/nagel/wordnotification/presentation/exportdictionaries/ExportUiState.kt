package com.nagel.wordnotification.presentation.exportdictionaries

import com.nagel.wordnotification.presentation.exportdictionaries.ExportVM.State

data class ExportUiState(
    var isStarted: Boolean = false,
    val loadingDiskState: State = State.Success,
    val isAuthorization: Boolean = false,
    val isAlgorithm: Boolean = false,
    val isLoadingFile: Boolean = false,
    val isExported: Boolean = false,
    val isAutoBackup: Boolean = false
)