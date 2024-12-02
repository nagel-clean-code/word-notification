package com.nagel.wordnotification.presentation.reader

import android.content.Context
import android.net.Uri
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.IFormationOfDictionaries
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.session.SessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringWriter
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
open class FileReader(
    private val dictionaryRepository: IFormationOfDictionaries,
) {

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    @ApplicationContext
    lateinit var context: Context

    private val myIdAccount: Long? by lazy { sessionRepository.getSession().account?.id }
    private var pos = 0

    suspend fun handleIntent(uri: Uri?, showResult: suspend (Int) -> Unit) {
        if (uri == null) return
        pos = 0
        var text: String? = null
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            text = getStringFromInputStream(inputStream)
            readStringAndCreateDictionaries(text)
            showResult(R.string.import_success)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
            showResult(R.string.could_not_open_file)
        }
        if (text == null) {
            showResult(R.string.could_not_open_file)
            return
        }
    }

    suspend fun readStringAndCreateDictionaries(str: String) {
        if (myIdAccount == null) return
        try {
            while (true) {
                val head = readHead(str)
                val dictionary =
                    dictionaryRepository.createDictionary(head.name + "(new)", myIdAccount!!)
                val words = readWords(str, dictionary.idDictionary)
                words.forEach { word ->
                    dictionaryRepository.addWord(word)
                }
                if (pos >= str.length - 1) return
            }
        } catch (_: Exception) {
        }
    }

    private fun readWords(str: String, idDictionary: Long): List<Word> {
        val words = mutableListOf<Word>()
        while ((str[pos++] == '~' || str[pos++] == '\n') && pos < str.length && str[pos] != '{') {
            val uuid = readWord(str).toInt()
            val textFirst = readWord(str)
            val textLast = readWord(str)
            val word = Word(idDictionary, textFirst, textLast, uniqueId = uuid)
            words.add(word)
        }
        return words
    }

    private fun readHead(str: String): Head {
        if (str[pos++] != '{') throw IOException()
        val name = readWord(str)
        val uuid = readWord(str)
        if (str[pos++] != '}') throw IOException()
        return Head(name, uuid)
    }


    private fun readWord(str: String): String {
        var word = ""
        if (str[pos++] != '|') throw IOException("${str[pos - 1]}, " + str.substring(0, pos - 1))
        var char = str[pos++]
        while (char != '|') {
            word += char
            if (pos >= str.length - 1) {
                throw IOException()
            }
            char = str[pos++]
        }
        return word.trim()
    }

    private fun getStringFromInputStream(stream: InputStream?): String {
        var n = 0
        val buffer = CharArray(1024 * 4)
        val reader = InputStreamReader(stream, "UTF8")
        val writer = StringWriter()
        while (-1 != reader.read(buffer).also { n = it }) writer.write(buffer, 0, n)
        return writer.toString()
    }

    inner class Head(val name: String, val idAuthor: String)
}