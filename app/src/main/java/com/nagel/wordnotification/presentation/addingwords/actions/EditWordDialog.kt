package com.nagel.wordnotification.presentation.addingwords.actions

import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.nagel.wordnotification.core.services.Utils
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.databinding.PopupEditWordBinding
import com.nagel.wordnotification.utils.GlobalFunction
import com.nagel.wordnotification.utils.RotationAnimator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class EditWordDialog(
    private val word: Word,
    private val success: () -> Unit
) : DialogFragment() {

    private lateinit var binding: PopupEditWordBinding

    @Inject
    lateinit var dictionaryRepository: DictionaryRepository

    private lateinit var rotationAnimatorReloadIcon: RotationAnimator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PopupEditWordBinding.inflate(inflater, container, false)
        setupTransparent()

        binding.apply {
            firstWord.setText(word.textFirst)
            lastWord.setText(word.textLast)
        }
        binding.root.postDelayed({
            focusInputAndAppearanceKeyboard()
        }, 200)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            rotationAnimatorReloadIcon =
                RotationAnimator(0, viewLifecycleOwner.lifecycleScope, swapIcon)
            swapIcon.setOnClickListener {
                swapWords()
                rotationAnimatorReloadIcon.rotationLeft()
            }
            saveButton.setOnClickListener {
                Utils.deleteNotification(word)
                word.textFirst = firstWord.text.toString()
                word.textLast = lastWord.text.toString()
                word.uniqueId = GlobalFunction.generateUniqueId()
                progressBar.isVisible = true
                lifecycleScope.launch(Dispatchers.IO) {
                    dictionaryRepository.updateText(word)
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        success.invoke()
                        dismiss()
                    }
                }
            }
        }
    }

    private fun swapWords() = with(binding) {
        val buf = firstWord.text
        firstWord.text = lastWord.text
        lastWord.text = buf
    }

    /** Фокусировка на ввод текста и автоматическое появление клавиатуры для ввода */
    private fun focusInputAndAppearanceKeyboard() {
        binding.firstWord.apply {
            requestFocus()
            val inputMethodManager =
                requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            setSelection(length())
        }
    }

    private fun setupTransparent() {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setStyle(STYLE_NO_FRAME, android.R.style.Theme)
    }

    companion object {
        const val TAG = "EditWordDialog"
    }
}