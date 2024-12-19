package com.nagel.wordnotification.presentation.exportAndImport

import android.content.Context
import android.net.Uri
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.analytecs.AppMetricaAnalytic
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.presentation.exportAndImport.ExportGenerator.Companion.FILE_FORMAT_TXT
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
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
    private val navigatorV2: NavigatorV2
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

    suspend fun handleIntent(
        uri: Uri?,
        showPremiumDialog: suspend (text: String, advertisementWasViewed: suspend () -> Unit) -> Unit
    ) {
        if (uri == null) return
        pos = 0
        var content: String? = null
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            content = getStringFromInputStream(inputStream)
            if (content.isBlank()) return
            try {
                if (uri.path?.contains(FILE_FORMAT_TXT) == true) {
                    txtReader.txtReader(content, showPremiumDialog)
                } else {
                    fireReader.fireReader(content, showPremiumDialog)
                }
            } catch (e: Exception) {
                AppMetricaAnalytic.reportEvent("file_reader_error_event")
                AppMetricaAnalytic.reportEvent("file_reader_error", mapOf("content" to content))
                throw e
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            navigatorV2.toast(R.string.could_not_open_file)
        } catch (e: IOException) {
            e.printStackTrace()
            navigatorV2.toast(R.string.could_not_open_file)
        } catch (e: Exception) {
            e.printStackTrace()
            navigatorV2.toast(R.string.could_not_open_file)
        }
        if (content == null) {
            navigatorV2.toast(R.string.could_not_open_file)
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