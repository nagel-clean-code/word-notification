package com.nagel.wordnotification.presentation.addingwords.actions

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.nagel.wordnotification.R
import com.nagel.wordnotification.databinding.MenuSelectingActionsOnWordBinding
import com.nagel.wordnotification.utils.common.showToast

class MenuSelectingActions(
    private val edit: (() -> Unit)? = null,
    private val delete: () -> Unit
) : DialogFragment() {

    private lateinit var binding: MenuSelectingActionsOnWordBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MenuSelectingActionsOnWordBinding.inflate(inflater, container, false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        setStyle(STYLE_NO_FRAME, android.R.style.Theme);
        binding.delete.setOnClickListener {
            delete.invoke()
            dismiss()
        }
        binding.edit.setOnClickListener {
            edit?.invoke() ?: run {
                showToast(R.string.in_process_implementation)
            }
            dismiss()
        }
        binding.move.setOnClickListener {
            showToast(R.string.in_process_implementation)
        }
        return binding.root
    }
}