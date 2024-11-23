package com.nagel.wordnotification.presentation

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.nagel.wordnotification.databinding.ConfirmationDialogBinding

class ConfirmationDialog(
    private val text: String,
    private val no: () -> Unit = {},
    private val yes: () -> Unit,
) : DialogFragment() {

    private lateinit var binding: ConfirmationDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ConfirmationDialogBinding.inflate(inflater, container, false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        setStyle(STYLE_NO_FRAME, android.R.style.Theme);

        binding.text.text = text
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() = with(binding) {
        yesButton.setOnClickListener {
            yes.invoke()
            dismiss()
        }
        noButton.setOnClickListener {
            no.invoke()
            dismiss()
        }
    }
}