package com.nagel.wordnotification.presentation.exportAndImport

import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

class CashReader @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val dictionaryRepository: DictionaryRepository,
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

    fun fireReader(content: String): List<Dictionary> {
        val dictionaries = mutableListOf<Dictionary>()
        while (content[pos] == '{' && pos + 1 < content.length - 1) {
            val dictionary = readDictionary(content)
            if (pos >= content.length) return dictionaries
            val words = readWords(content)
            dictionary.wordList = words.toMutableList()
            dictionaries.add(dictionary)
        }
        return dictionaries
    }

    private fun readWords(str: String): List<Word> {
        val wordList = mutableListOf<Word>()
        while (pos < str.length && str[pos++] == 'w') {
            val textFirst = readWord(str)
            val textLast = readWord(str)
            wordList.add(Word(0, textFirst, textLast))
        }
        --pos
        return wordList
    }

    private fun readDictionary(str: String): Dictionary {
        if (str[pos++] != '{') throw IOException()
        var name = readWord(str)
        val dateCreated = readWord(str).toLong()
        val idFolder = readWord(str).toLong()
        val include = readWord(str).toBoolean()

        name = name.ifBlank { navigatorV2.getString(R.string.dictionary) }
        while (currentDictionariesNames.contains(name)) {
            name += "(new)"
        }

        val newDictionary = Dictionary(
            idDictionary = 0,
            idAuthor = myIdAccount!!,
            name = name,
            dateCreated = dateCreated,
            idFolder = idFolder,
            idMode = 0,
            include = include
        )

        if (str[pos++] != '}') throw IOException()
        return newDictionary
    }

    private fun readWord(str: String): String {
        var word = ""
        if (str[pos++] != '|') throw IOException("${str[pos - 1]}, " + str.substring(0, pos - 1))
        var char = str[pos++]
        while (char != '|') {
            word += char
            if (pos > str.length - 1) {
                throw IOException()
            }
            char = str[pos++]
        }
        return word.trim()
    }
}