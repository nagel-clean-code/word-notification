package com.nagel.wordnotification.presentation.addingwords.worddetails

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.entities.NotificationHistoryItem
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.databinding.WordDetailsDialogBinding
import com.nagel.wordnotification.presentation.addingwords.worddetails.widget.model.ShowStepsWordDto
import com.nagel.wordnotification.presentation.base.ErrorResult
import com.nagel.wordnotification.presentation.base.PendingResult
import com.nagel.wordnotification.presentation.base.SuccessResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WordDetailsDialog(
    private val word: Word,
    private val idMode: Long? = null,
    private val isAlgorithmEnabled: Boolean,
    private val modeSettingsDto: ModeSettingsDto? = null
) : DialogFragment() {

    private lateinit var binding: WordDetailsDialogBinding
    private val viewModel: WordDetailsVM by viewModels()

    private fun isDemonstrationOfIntervals() = idMode == null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = WordDetailsDialogBinding.inflate(inflater, container, false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setStyle(STYLE_NO_FRAME, android.R.style.Theme)
        if (isDemonstrationOfIntervals()) {
            binding.name.isVisible = false
            binding.convertDatesIcon.isVisible = false
        } else {
            binding.name.text = word.textFirst + " - " + word.textLast
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        if (isDemonstrationOfIntervals()) {
            modeSettingsDto?.let {
                initDate(modeSettingsDto)
            }
        } else {
            idMode?.let { viewModel.loadMode(idMode) }
        }
    }

    private fun initListeners() = with(binding) {
        convertDatesIcon.setOnClickListener {
            val icon = if (convertDatesIcon.tag as? Boolean == true) {
                convertDatesIcon.tag = false
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.swop_right
                )
            } else {
                convertDatesIcon.tag = true
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.swop_left
                )
            }
            convertDatesIcon.setImageDrawable(icon)

            showSteps.displayStepsIn(isWords = convertDatesIcon.tag as? Boolean == true)
        }
        okButton.setOnClickListener {
            dismiss()
        }
        viewModel.loadModeStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is PendingResult -> {
                    progressBar.isVisible = true
                    showSteps.isVisible = false
                }

                is ErrorResult -> {
                    Log.e(TAG, status.exception.toString(), status.exception)
                    showMessage(R.string.first_set_up_mode)
                    dismiss()
                }

                is SuccessResult -> {
                    if (status.data != null) {
                        initDate(status.data)
                        progressBar.isVisible = false
                        showSteps.isVisible = true
                    } else {
                        showMessage(R.string.first_set_up_mode)
                        dismiss()
                    }
                }
            }
        }
    }

    private fun initDate(mode: ModeSettingsDto) {
        val data: ShowStepsWordDto = word.toShowStepsWordDto(mode)
        var algorithmName = requireContext().getString(R.string.algorithm)
        if (data.mode.selectedMode != null) {
            algorithmName += " " + data.mode.selectedMode.getName(requireContext())
            binding.algorithmName.text = algorithmName
        }
        val historyFlow = viewModel.loadNotificationHistory(word.idWord, mode.idMode)
        if (isDemonstrationOfIntervals().not()) {
            initShowSteps(data, historyFlow)
        } else {
            data.historyList = emptyList()
            binding.showSteps.setData(data, isAlgorithmEnabled)
            binding.showSteps.displayStepsIn(true)
        }
    }

    private fun initShowSteps(
        data: ShowStepsWordDto,
        historyFlow: Flow<List<NotificationHistoryItem>?>
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            historyFlow.collect() { historyList ->
                historyList?.let {
                    data.historyList = historyList
                    binding.showSteps.setData(data, isAlgorithmEnabled)
                }
            }
        }
    }

    private fun showMessage(msgId: Int) {
        val msg = requireContext().getString(msgId)
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "WordDetailsDialog"
    }
}