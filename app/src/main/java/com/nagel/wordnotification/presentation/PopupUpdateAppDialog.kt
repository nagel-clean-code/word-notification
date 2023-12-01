package com.nagel.wordnotification.presentation

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.nagel.wordnotification.databinding.PopupUpdateAppBinding
import com.nagel.wordnotification.utils.GlobalFunction.openUrl


class PopupUpdateAppDialog(
    private val mandatory: Boolean,
    private val link: String
) : DialogFragment() {

    private lateinit var binding: PopupUpdateAppBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PopupUpdateAppBinding.inflate(inflater, container, false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setStyle(STYLE_NO_FRAME, android.R.style.Theme)
        binding.laterButton.isVisible = !mandatory
        isCancelable = !mandatory
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            updateButton.setOnClickListener {
                requireContext().openUrl(link)
            }
            laterButton.setOnClickListener {
                dismiss()
            }
        }
    }

    companion object {
        const val TAG = "PopupUpdateAppDialog"
    }
}