package com.nagel.wordnotification.presentation.randomizer

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.nagel.wordnotification.databinding.ResultRandomizingDialogBinding

class ResultRandomizingFragmentDialog(
    private val remembers: Int,
    private val countWords: Int,
    private val closeDialog: () -> Unit
) : DialogFragment() {

    private lateinit var binding: ResultRandomizingDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ResultRandomizingDialogBinding.inflate(inflater, container, false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        setStyle(STYLE_NO_FRAME, android.R.style.Theme);

        binding.remember.text = remembers.toString()
        binding.size.text = countWords.toString()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    override fun onDestroy() {
        closeDialog.invoke()
        super.onDestroy()
    }

    private fun initListeners() {
        binding.repeat.setOnClickListener {
            dismiss()
        }
    }

}