package com.nagel.wordnotification.presentation.choosingdictionary

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.firbase.RemoteDbRepository
import com.nagel.wordnotification.databinding.MenuForDictionaryBinding
import dagger.hilt.android.AndroidEntryPoint
import io.appmetrica.analytics.AppMetrica
import javax.inject.Inject

@AndroidEntryPoint
class MenuForDictionaryDialog(
    private val dictionary: Dictionary,
    private val edit: ((Dictionary) -> Unit)? = null,
    private val copy: (Dictionary) -> Unit,
    private val exportDictionary: (Dictionary) -> Unit,
    private val exportAllDictionary: () -> Unit,
    private val delete: () -> Unit,
) : DialogFragment() {

    @Inject
    lateinit var realtimeDb: RemoteDbRepository

    private lateinit var binding: MenuForDictionaryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MenuForDictionaryBinding.inflate(inflater, container, false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        setStyle(STYLE_NO_FRAME, android.R.style.Theme);
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() = with(binding) {
        delete.setOnClickListener {
            this@MenuForDictionaryDialog.delete.invoke()
            dismiss()
        }
        edit.setOnClickListener {
            this@MenuForDictionaryDialog.edit?.invoke(dictionary)
            dismiss()
        }
        exportDictionary.setOnClickListener {
            AppMetrica.reportEvent("export_on_dictionary_click")
            if (realtimeDb.isTesting()) return@setOnClickListener
            this@MenuForDictionaryDialog.exportDictionary.invoke(dictionary)
        }
        exportAll.setOnClickListener {
            AppMetrica.reportEvent("export_all_dictionaries_click")
            if (realtimeDb.isTesting()) return@setOnClickListener
            this@MenuForDictionaryDialog.exportAllDictionary()
        }
        copyDictionary.setOnClickListener {
            copy.invoke(dictionary)
            dismiss()
        }
    }
}