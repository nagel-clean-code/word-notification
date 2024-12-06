package com.nagel.wordnotification.presentation.exportdictionaries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.firbase.RemoteDbRepository
import com.nagel.wordnotification.data.premium.PremiumRepository
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.databinding.FragmentExportDictionariesBinding
import com.nagel.wordnotification.presentation.base.BaseFragment
import com.nagel.wordnotification.presentation.navigator.BaseScreen
import com.nagel.wordnotification.presentation.premiumdialog.PremiumDialog
import com.nagel.wordnotification.utils.common.sendFile
import com.nagel.wordnotification.utils.common.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExportFragment : BaseFragment() {
    class Screen : BaseScreen

    @Inject
    lateinit var remoteDbRepository: RemoteDbRepository

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var premiumRepository: PremiumRepository

    private lateinit var binding: FragmentExportDictionariesBinding
    override val viewModel: ExportVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExportDictionariesBinding.inflate(inflater, container, false)

        initListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.isStarted = premiumRepository.getIsStarted()
        binding.saveAlgorithm.isChecked = viewModel.isStarted
    }

    private fun initListeners() = with(binding) {
        saveAlgorithm.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && viewModel.isStarted.not()) {
                saveAlgorithm.isChecked = false
                PremiumDialog(
                    text = resources.getString(R.string.dictionary_export_is_available),
                    isChoiceAdvertisement = false,
                ).show(childFragmentManager, PremiumDialog.TAG)
            }
        }
        backupDiskButton.setOnClickListener {
            showToast(R.string.in_process_implementation)
            if (viewModel.isStarted.not()) {
                showToast(R.string.it_will_be_available_only_in_premium)
            }
        }
        exportButton.setOnClickListener {
            viewModel.writeDictionaries(saveAlgorithm.isChecked, requireActivity()::sendFile)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ExportFragment()
    }
}