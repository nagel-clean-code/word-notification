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
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.databinding.WordDetailsDialogBinding
import com.nagel.wordnotification.presentation.base.ErrorResult
import com.nagel.wordnotification.presentation.base.PendingResult
import com.nagel.wordnotification.presentation.base.SuccessResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WordDetailsDialog(val word: Word) : DialogFragment() {

    private lateinit var binding: WordDetailsDialogBinding
    private val viewModel: WordDetailsVM by viewModels()

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

        binding.name.text = word.textFirst + " - " + word.textLast
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        viewModel.loadMode(word.idDictionary)
    }

    private fun initListeners() {
        binding.apply {
            okButton.setOnClickListener {
                dismiss()
            }
            viewModel.liveResult.observe(viewLifecycleOwner) { status ->
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
                            val data = word.toShowStepsWordDto(status.data)
                            showSteps.setData(data)
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
    }

    private fun showMessage(msgId: Int) {
        val msg = requireContext().getString(msgId)
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "WordDetailsDialog"
    }
}