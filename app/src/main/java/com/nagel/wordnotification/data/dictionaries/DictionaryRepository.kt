package com.nagel.wordnotification.data.dictionaries

import com.nagel.wordnotification.data.dictionaries.entities.Word

interface DictionaryRepository {
    fun loadDictionary(name: String, idAuthor: Long, success: (Boolean) -> Unit)
    fun getSize(): Int
    fun getItem(position: Int): Word
    fun addWord(textFirst: String, textLast: String)
    fun createDictionary(name: String, idAccount: Long, success: () -> Unit)
}