package com.nagel.wordnotification.presentation.addingwords.choicelanguage

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
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
    private lateinit var languageSelected: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChoiceLanguageBinding.inflate(inflater, container, false)
        setupTransparent()
        languageSelected = sessionRepository.getTranslationLanguage()
        binding.root.hideKeyboard()
        val currentLangText =
            requireContext().getString(R.string.current_language, languageSelected)
        binding.currentLanguage.text = currentLangText
        binding.autoTranslation.isChecked = sessionRepository.getAutoTranslation()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            done.setOnClickListener {
                sessionRepository.saveTranslationLanguage(languageSelected)
                langSelected.invoke(languageSelected, autoTranslation.isChecked)
                dismiss()
            }
            autoTranslation.setOnClickListener {
                sessionRepository.saveAutoTranslation(autoTranslation.isChecked)
            }
            closeButton.setOnClickListener {
                dismiss()
            }
        }
        initAdapter()
    }

    private fun initAdapter() {
        val adapter = LanguagesAdapter(languageSelected, list) { pos ->
            languageSelected = list[pos]
        }
        binding.listLanguages.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext())

            var pos = list.indexOf(languageSelected)
            pos -= if (pos >= 5) 5 else pos
            scrollToPosition(pos)
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