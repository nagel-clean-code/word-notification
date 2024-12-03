package com.nagel.wordnotification.presentation.exportAndImport

import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

class TxtReader @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val sessionRepository: SessionRepository,
    private val navigatorV2: NavigatorV2
) {

    private val myIdAccount: Long? by lazy { sessionRepository.getSession().account?.id }
    private lateinit var currentDictionariesNames: List<String>
    private var pos = 0

    init {
        MainScope().launch(Dispatchers.IO) {
            myIdAccount?.let { id ->
                currentDictionariesNames = dictionaryRepository.loadDictionaries(id).map { it.name }
            }
        }
    }

    suspend fun txtReader(content: String, dictionaryName: String) {
        if (myIdAccount == null) return
        var name = dictionaryName.ifBlank { navigatorV2.getString(R.string.dictionary) }
        while (currentDictionariesNames.contains(name)) {
            name += "(new)"
        }
        val dictionary = dictionaryRepository.createDictionary(name, myIdAccount!!)
        val words = readWordsTxt(content, dictionary.idDictionary)
        words.forEach { word ->
            dictionaryRepository.addWord(word)
        }
    }

    private fun readWordsTxt(str: String, idDictionary: Long): List<Word> {
        val words = mutableListOf<Word>()
        while ((str[pos] == '[') && pos < str.length) {
            val textFirst = readTextFirst(str)
            val textLast = readTextLast(str)
            words.add(Word(idDictionary, textFirst, textLast))
        }
        return words
    }

    private fun readTextFirst(str: String): String {
        var word = ""
        if (str[pos++] != '[') throw IOException("${str[pos - 1]}, " + str.substring(0, pos - 1))
        var char = str[pos++]
        while (char != ';') {
            word += char
            if (pos >= str.length - 1) {
                throw IOException()
            }
            char = str[pos++]
        }
        return word.trim()
    }

    private fun readTextLast(str: String): String {
        var word = ""
        if (str[pos] != ';') throw IOException("${str[pos - 1]}, " + str.substring(0, pos - 1))
        var char = str[pos++]
        while (char != ']') {
            word += char
            if (pos >= str.length - 1) {
                throw IOException()
            }
            char = str[pos++]
        }
        return word.trim()
    }
}