package com.nagel.wordnotification.presentation.addingwords.choicelanguage

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.databinding.ChoiceLanguageBinding
import com.nagel.wordnotification.utils.common.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import me.bush.translator.Language
import javax.inject.Inject


@AndroidEntryPoint
class ChoiceLanguageDialog(
    private val langSelected: (String?, Boolean) -> Unit
) : DialogFragment() {

    @Inject
    lateinit var sessionRepository: SessionRepository

    private lateinit var binding: ChoiceLanguageBinding
    private val list = Language.values().filter { it.code != "auto" }.map { it.name }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChoiceLanguageBinding.inflate(inflater, container, false)
        setupTransparent()
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            R.layout.language_item,
            list
        )
        binding.listLanguages.adapter = adapter
        binding.root.hideKeyboard()
        val lang = sessionRepository.getTranslationLanguage()
        val currentLangText = requireContext().getString(R.string.current_language, lang)
        binding.currentLanguage.text = currentLangText
        binding.autoTranslation.isChecked = sessionRepository.getAutoTranslation()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            cancelButton.setOnClickListener {
                langSelected.invoke(null, autoTranslation.isChecked)
                dismiss()
            }
            listLanguages.setOnItemClickListener() { _, _, position: Int, _ ->
                val lang = list[position]
                sessionRepository.saveTranslationLanguage(lang)
                langSelected.invoke(lang, autoTranslation.isChecked)
                dismiss()
            }
            autoTranslation.setOnClickListener {
                sessionRepository.saveAutoTranslation(autoTranslation.isChecked)
            }
        }
    }


    private fun setupTransparent() {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setStyle(STYLE_NO_FRAME, android.R.style.Theme)
    }

    companion object {
        const val TAG = "ChoiceLanguageDialog"
    }
}