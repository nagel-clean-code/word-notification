package com.nagel.wordnotification.presentation.settings

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.nagel.wordnotification.databinding.RequestPermissionsDialogBinding

class RequestPermissionDialog(
    private val text: String,
    private val exitButtonClick: () -> Unit,
    private val provideButtonClick: (() -> Unit)? = null,
    private var onDestroy: (() -> Unit)? = null
) : DialogFragment() {

    private lateinit var binding: RequestPermissionsDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RequestPermissionsDialogBinding.inflate(inflater, container, false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        setStyle(STYLE_NO_FRAME, android.R.style.Theme);

        binding.text.text = text
        if (provideButtonClick == null) {
            binding.provideButton.isVisible = false
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() = with(binding) {
        provideButton.setOnClickListener {
            onDestroy = null
            dismiss()
            provideButtonClick?.invoke()
        }
        exitButton.setOnClickListener {
            onDestroy = null
            dismiss()
            exitButtonClick.invoke()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onDestroy?.invoke()
    }

    companion object{
        const val TAG = "RequestPermissionDialog"
    }
}