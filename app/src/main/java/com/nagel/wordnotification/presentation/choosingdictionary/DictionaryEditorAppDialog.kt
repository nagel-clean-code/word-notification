package com.nagel.wordnotification.presentation.choosingdictionary

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.nagel.wordnotification.R
import com.nagel.wordnotification.databinding.PopupEditDictionaryBinding
import com.nagel.wordnotification.utils.common.showKeyboard


class DictionaryEditorAppDialog(
    private val name: String? = null,
    private val returnName: (String) -> Unit
) : DialogFragment() {

    private lateinit var binding: PopupEditDictionaryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PopupEditDictionaryBinding.inflate(inflater, container, false)
        setupTransparent()
        setupPositionFragmentDialog()

        binding.apply {
            name?.let {
                dictionaryName.setText(it)
                saveButton.text = requireContext().getString(R.string.save)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            saveButton.setOnClickListener {
                val name = dictionaryName.text.toString().trim()
                returnName.invoke(name)
                dismiss()
            }
            dictionaryName.apply {
                showKeyboard()
                setSelection(length())
            }
        }
    }

    private fun setupTransparent() {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setStyle(STYLE_NO_FRAME, android.R.style.Theme)
    }

    private fun setupPositionFragmentDialog() {
        dialog?.window?.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
        val layoutParams = dialog?.window?.attributes
        layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams?.y = 50
        dialog?.window?.attributes = layoutParams
    }

    companion object {
        const val TAG = "DictionaryEditorAppDialog"
    }
}