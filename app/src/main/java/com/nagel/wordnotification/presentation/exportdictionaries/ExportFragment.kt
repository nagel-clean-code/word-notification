package com.nagel.wordnotification.presentation.exportdictionaries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.databinding.FragmentExportDictionariesBinding
import com.nagel.wordnotification.presentation.ConfirmationDialog
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import com.nagel.wordnotification.presentation.navigator.navigator
import com.nagel.wordnotification.presentation.premiumdialog.CommonInformationDialog
import com.nagel.wordnotification.presentation.premiumdialog.PremiumDialog
import com.nagel.wordnotification.utils.common.collectStarted
import com.nagel.wordnotification.utils.common.sendFile
import com.nagel.wordnotification.utils.common.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExportFragment : BaseFragment() {
    class Screen : BaseScreen

    @Inject
    lateinit var sessionRepository: SessionRepository

    private lateinit var binding: FragmentExportDictionariesBinding
    override val viewModel: ExportVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExportDictionariesBinding.inflate(inflater, container, false)
        initStates()
        initListeners()
        return binding.root
    }

    private fun initListeners() = with(binding) {
        saveAlgorithm.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && viewModel.state.value.isStarted.not()) {
                saveAlgorithm.isChecked = false
                PremiumDialog(
                    text = resources.getString(R.string.dictionary_export_is_available),
                    isChoiceAdvertisement = false,
                ).show(childFragmentManager, PremiumDialog.TAG)
            } else {
                viewModel.changeIsAlgorithmChecked(saveAlgorithm.isChecked)
            }
        }
        backupDiskButton.setOnClickListener {
            backupDiskButtonOnClick()
        }
        exportButton.setOnClickListener {
            exportButtonClick()
        }
        listOfCopiesButton.setOnClickListener {
            showToast(R.string.in_process_implementation)
//            navigator()?.showGoogleDiskFilesFragment()
        }
        infoButton.setOnClickListener {
            CommonInformationDialog(
                text = resources.getString(R.string.google_drive_auto_copying_information)
            ).show(childFragmentManager, CommonInformationDialog.TAG)
        }
    }

    private fun backupDiskButtonOnClick() {
        if (viewModel.state.value.isAuthorization.not()) {
            if (viewModel.state.value.isStarted.not()) {
                showToast(R.string.it_will_be_available_only_in_premium)
            } else {
                viewModel.signIn()
            }
        } else {
            viewModel.changeIsAutoBackUp()
        }
    }

    private fun exportButtonClick() {
        if (viewModel.state.value.isAuthorization) {
            ConfirmationDialog(
                text = resources.getString(R.string.send_file_or_upload_to_google_drive),
                yes = ::writeDictionaries,
                no = ::uploadToGoogleDrive,
                textYes = resources.getString(R.string.a_file),
                textNo = resources.getString(R.string.a_google_drive),
                variant = 2
            ).show(childFragmentManager, ConfirmationDialog.TAG)
        } else {
            writeDictionaries()
        }
    }

    private fun uploadToGoogleDrive() {
        viewModel.uploadBackupToGoogleDisk()
    }

    private fun writeDictionaries() {
        viewModel.writeDictionaries() { file ->
            requireActivity().sendFile(file)
            viewModel.changeExported(true)
        }
    }

    private fun initStates() {
        viewModel.state.collectStarted(viewLifecycleOwner, ::renderState)
    }

    private fun renderState(state: ExportUiState) = with(binding) {
        saveAlgorithm.isChecked = state.isAlgorithm
        handleLoadingGoogleDisk(state.loadingDiskState)
        backupDiskButton.text = if (state.isAutoBackup) {
            resources.getString(R.string.unplug)
        } else {
            resources.getString(R.string.include)
        }
        progressBar.isVisible =
            state.loadingDiskState is ExportVM.State.Loading || state.isLoadingFile
        if (state.isLoadingFile) {
            backupDiskButton.text = "       "
        }
        if (state.isExported) {
            navigator()?.goBack()
        }
    }

    private fun handleLoadingGoogleDisk(state: ExportVM.State) = with(binding) {
        progressBar.isVisible = state is ExportVM.State.Loading
        if (state is ExportVM.State.Loading) {
            backupDiskButton.text = "       "
        }
        error.isVisible = state is ExportVM.State.Error
        backupDiskButton.isGone = state is ExportVM.State.Error
        headerTitle.isVisible = state !is ExportVM.State.Error
        if (state is ExportVM.State.Success) {
            backupDiskButton.text = resources.getString(R.string.unplug)
            listOfCopiesButton.isVisible = true
            infoButton.isVisible = true
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ExportFragment()
    }
}