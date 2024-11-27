package com.nagel.wordnotification.presentation.settings

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.nagel.wordnotification.core.algorithms.Algorithm
import com.nagel.wordnotification.core.algorithms.ForgetfulnessCurveLong
import com.nagel.wordnotification.core.algorithms.ForgetfulnessCurveShort
import com.nagel.wordnotification.core.algorithms.PlateauEffect
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.databinding.InformationDialogBinding
import com.nagel.wordnotification.presentation.addingwords.worddetails.WordDetailsDialog

class InformationDialog() : DialogFragment() {

    private lateinit var binding: InformationDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = InformationDialogBinding.inflate(inflater, container, false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setStyle(STYLE_NO_FRAME, android.R.style.Theme)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() = with(binding) {
        okButton.setOnClickListener {
            dismiss()
        }
        showIntervals1.setOnClickListener {
            showWordDetailsDialog(ForgetfulnessCurveShort)
        }
        showIntervals2.setOnClickListener {
            showWordDetailsDialog(ForgetfulnessCurveLong)
        }
        showIntervals3.setOnClickListener {
            showWordDetailsDialog(PlateauEffect)
        }
    }

    private fun showWordDetailsDialog(algorithm: Algorithm) {
        WordDetailsDialog(
            word = Word.createEmptyWord(),
            isAlgorithmEnabled = true,
            modeSettingsDto = ModeSettingsDto.createEmptyModeSettingsDto(algorithm)
        ).show(childFragmentManager, WordDetailsDialog.TAG)
    }

    companion object {
        const val TAG = "InformationDialog"
    }
}