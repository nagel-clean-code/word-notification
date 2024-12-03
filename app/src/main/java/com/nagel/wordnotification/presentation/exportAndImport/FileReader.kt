package com.nagel.wordnotification.presentation.exportAndImport

import android.content.Context
import android.net.Uri
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.presentation.exportAndImport.ExportGenerator.Companion.FILE_FORMAT_TXT
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringWriter
import javax.inject.Inject

open class FileReader @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val sessionRepository: SessionRepository,
    @ApplicationContext private val context: Context,
    private val txtReader: TxtReader,
    private val fireReader: FireReader,
) {
    private val myIdAccount: Long? by lazy { sessionRepository.getSession().account?.id }
    private lateinit var currentDictionariesNames: List<String>

    init {
        MainScope().launch(Dispatchers.IO) {
            myIdAccount?.let { id ->
                currentDictionariesNames = dictionaryRepository.loadDictionaries(id).map { it.name }
            }
        }
    }

    private var pos = 0

    suspend fun handleIntent(uri: Uri?, showResult: suspend (Int) -> Unit) {
        if (uri == null) return
        pos = 0
        var content: String? = null
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            content = getStringFromInputStream(inputStream)
            if (content.isBlank()) return
            if (uri.path?.contains(FILE_FORMAT_TXT) == true) {
                txtReader.txtReader(content, uri.fragment ?: "СЛОВАРЬ") //TODO проверить на тесте
            } else {
                fireReader.fireReader(content)
            }
            showResult(R.string.import_success)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
            showResult(R.string.could_not_open_file)
        }
        if (content == null) {
            showResult(R.string.could_not_open_file)
            return
        }
    }

    private fun getStringFromInputStream(stream: InputStream?): String {
        var n = 0
        val buffer = CharArray(1024 * 4)
        val reader = InputStreamReader(stream, "UTF8")
        val writer = StringWriter()
        while (-1 != reader.read(buffer).also { n = it }) writer.write(buffer, 0, n)
        return writer.toString()
    }
}