package com.nagel.wordnotification.presentation.exportAndImport

import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.firbase.RemoteDbRepository
import com.nagel.wordnotification.data.premium.PremiumRepository
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import com.nagel.wordnotification.utils.Toggles
import java.io.IOException
import javax.inject.Inject

class TxtReader @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val sessionRepository: SessionRepository,
    private val premiumRepository: PremiumRepository,
    private val navigatorV2: NavigatorV2,
    dbRepository: RemoteDbRepository
) {

    private val myIdAccount: Long? by lazy { sessionRepository.getSession().account?.id }
    private var isStarted: Boolean = false
    private var limitWords: Int = 0
    private lateinit var currentDictionariesNames: List<String>
    private var pos = 0
    private val charactersToSkip = listOf(' ', '\n', '\r', ',', ';', '.')
    private var curentIxAddWord = 0
    private var currentWord = 0
    private val addNumberFreeWords = premiumRepository.getAddNumberFreeWords()
    private var isAdvToggle = false

    init {
        dbRepository.getFeatureToggles(success = { toggles ->
            isAdvToggle = toggles.content.contains(Toggles.Adv.name)
        })
    }

    suspend fun txtReader(
        content: String,
        showPremiumDialog: suspend (text: String, advertisementWasViewed: suspend () -> Unit) -> Unit
    ) {
        myIdAccount?.let { id ->
            currentDictionariesNames = dictionaryRepository.loadDictionaries(id).map { it.name }
        }
        limitWords = premiumRepository.getCurrentLimitWord()
        currentWord = dictionaryRepository.getAllWords().size
        isStarted = premiumRepository.getIsStarted()
        curentIxAddWord = 0
        pos = 0
        if (myIdAccount == null) return
        var name = navigatorV2.getString(R.string.dictionary)
        while (currentDictionariesNames.contains(name)) {
            name += " (new)"
        }
        val dictionary = dictionaryRepository.createDictionary(name, myIdAccount!!)
        val words = readWordsTxt(content, dictionary.idDictionary)
        addWords(words, showPremiumDialog)
    }

    private suspend fun addWords(
        words: List<Word>,
        showPremiumDialog: suspend (text: String, advertisementWasViewed: suspend () -> Unit) -> Unit
    ) {
        while (curentIxAddWord < words.size) {
            if (isStarted.not() && currentWord + curentIxAddWord >= limitWords) {
                val text = if (isAdvToggle) {
                    val textL = navigatorV2.getString(R.string.suggestion_of_additional_words_d_d)
                    String.format(textL, addNumberFreeWords, curentIxAddWord, words.size)
                } else {
                    navigatorV2.getString(R.string.suggestion_of_additional_words_only_premium)
                }
                premiumRepository.saveCurrentLimitWords(currentWord + curentIxAddWord)
                showPremiumDialog.invoke(text) {
                    limitWords += addNumberFreeWords
                    premiumRepository.saveCurrentLimitWords(limitWords)
                    addWords(words, showPremiumDialog)
                }
                return
            } else {
                dictionaryRepository.addWord(words[curentIxAddWord++])
            }
        }
        premiumRepository.saveCurrentLimitWords(limitWords)
        navigatorV2.toast(R.string.import_success)
    }

    private fun readWordsTxt(str: String, idDictionary: Long): List<Word> {
        val words = mutableListOf<Word>()
        while (pos < str.length && charactersToSkip.contains(str[pos])) {
            ++pos
        }
        while (pos < str.length && str[pos] == '[') {
            val textFirst = readTextFirst(str)
            val textLast = readTextLast(str)
            words.add(Word(idDictionary, textFirst, textLast))
            while (pos < str.length && charactersToSkip.contains(str[pos])) {
                ++pos
            }
        }
        return words
    }

    private fun readTextFirst(str: String): String {
        var word = ""
        if (str[pos++] != '[') throw IOException("${str[pos - 1]}, " + str.substring(0, pos - 1))
        var char = str[pos++]
        while (char != ';') {
            word += char
            if (pos >= str.length) {
                throw IOException()
            }
            char = str[pos++]
        }
        return word.trim()
    }

    private fun readTextLast(str: String): String {
        var word = ""
        var char = str[pos++]
        while (char != ']') {
            word += char
            if (pos >= str.length) {
                throw IOException()
            }
            char = str[pos++]
        }
        return word.trim()
    }
}