package com.nagel.wordnotification.presentation.choosingdictionary

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.nagel.wordnotification.BuildConfig
import com.nagel.wordnotification.R
import com.nagel.wordnotification.app.App
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.firbase.RealtimeDbRepository
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.databinding.MenuForDictionaryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MenuForDictionaryDialog(
    private val dictionary: Dictionary,
    private val edit: ((Dictionary) -> Unit)? = null,
    private val copy: (Dictionary) -> Unit,
    private val delete: () -> Unit,
) : DialogFragment() {

    private lateinit var binding: MenuForDictionaryBinding

    @Inject
    lateinit var repository: DictionaryRepository

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var realtimeDb: RealtimeDbRepository

    private var idAuthorUUID: String? = null
    private var accountId: Long? = null
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
        lifecycleScope.launch {
            idAuthorUUID = sessionRepository.getSession()?.account?.idAuthorUUID
            accountId = sessionRepository.getSession()?.account?.id
        }
        initListeners()
    }

    private fun initListeners() {
        binding.delete.setOnClickListener {
            delete.invoke()
            dismiss()
        }
        binding.edit.setOnClickListener {
            edit?.invoke(dictionary)
            dismiss()
        }
        binding.exportDictionary.setOnClickListener {
            if (realtimeDb.isTesting()) return@setOnClickListener
            exportDictionary()
        }
        binding.exportAll.setOnClickListener {
            if (realtimeDb.isTesting()) return@setOnClickListener
            exportAll()
        }
        binding.copyDictionary.setOnClickListener {
            copy.invoke(dictionary)
            dismiss()
        }
    }

    private fun exportAll() {
        lifecycleScope.launch(Dispatchers.IO) {
            val file = createFile(true)
            val dictionaries = repository.loadDictionaries(accountId!!)
            writeDictionaries(dictionaries, file)
            withContext(Dispatchers.Main) {
                sendFile(file)
            }
        }
    }

    private fun exportDictionary() {
        lifecycleScope.launch(Dispatchers.IO) {
            val file = createFile(false)
            writeDictionaries(listOf(dictionary), file)
            withContext(Dispatchers.Main) {
                sendFile(file)
            }
        }
    }

    private fun createFile(all: Boolean): File {
        val name = if (all) {
            requireContext().getString(R.string.my_dictionaries)
        } else {
            dictionary.name
        }
        val file = File(App.get().filesDir, "$name.fire")
        deleteFile = file
        return file
    }

    private fun writeDictionaries(dictionaries: List<Dictionary>, file: File) {
        file.printWriter().use { out ->
            dictionaries.forEach() { current ->
                out.print("{|${current.name}||$idAuthorUUID|}~")
                current.wordList.forEach {
                    it.apply {
                        out.print("|$uniqueId||$textFirst||$textLast|~")
                    }
                }
            }
        }
    }

    private var deleteFile: File? = null
    private fun sendFile(file: File) {
        try {
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.APPLICATION_ID + ".provider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.type = "*/*"
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteFile?.delete()
    }
}