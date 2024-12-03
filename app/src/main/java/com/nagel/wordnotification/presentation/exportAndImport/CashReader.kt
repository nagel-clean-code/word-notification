package com.nagel.wordnotification.presentation.exportAndImport

import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import java.io.IOException
import javax.inject.Inject

class CashReader @Inject constructor() {

    private var pos = 0

    fun fireReader(content: String): List<Dictionary> {
        val dictionaries = mutableListOf<Dictionary>()
        while (pos + 1 < content.length - 1) {
            val name = readDictionaryName(content)
            val dictionary = Dictionary.createEmpty(name)
            val words = readWords(content)
            dictionary.wordList = words.toMutableList()
            dictionaries.add(dictionary)
        }
        return dictionaries
    }

    private fun readWords(str: String): List<Word> {
        val wordList = mutableListOf<Word>()
        while (str[pos++] == 'w' && pos < str.length - 1) {
            val textFirst = readWord(str)
            val textLast = readWord(str)
            wordList.add(Word(0, textFirst, textLast))
        }
        --pos
        return wordList
    }

    private fun readDictionaryName(str: String): String {
        if (str[pos++] != '{') throw IOException()
        val name = readWord(str)
        if (str[pos++] != '}') throw IOException()
        return name
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
}