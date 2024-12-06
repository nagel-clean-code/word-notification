package com.nagel.wordnotification.presentation.exportAndImport

import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.NotificationHistoryItem
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.premium.PremiumRepository
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.room.entities.ModeDbEntity
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import java.io.IOException
import javax.inject.Inject

class FireReader @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
    private val premiumRepository: PremiumRepository,
    private val navigatorV2: NavigatorV2,
) {

    private val myIdAccount: Long? by lazy { sessionRepository.getSession().account?.id }
    private lateinit var currentDictionariesNames: List<String>
    private var isStarted: Boolean = false

    private var pos = 0
    private var limitWords: Int = 0
    private var curentIxAddWord = 0
    private var currentWord = 0
    private val addNumberFreeWords = premiumRepository.getAddNumberFreeWords()

    suspend fun fireReader(
        content: String,
        showPremiumDialog: suspend (text: String, advertisementWasViewed: suspend () -> Unit) -> Unit
    ) {
        if (myIdAccount == null) return
        myIdAccount?.let { id ->
            currentDictionariesNames = dictionaryRepository.loadDictionaries(id).map { it.name }
        }
        isStarted = premiumRepository.getIsStarted()
        pos = 0
        while (content[pos] == '{' && pos + 1 < content.length - 1) {
            limitWords = premiumRepository.getCurrentLimitWord()
            currentWord = dictionaryRepository.getAllWords().size
            val dictionary = readDictionary(content)
            val idDictionary = dictionaryRepository.saveDictionary(dictionary)
            dictionary.idDictionary = idDictionary
            if (pos >= content.length) {
                navigatorV2.toast(R.string.import_success)
                return
            }
            val isAlgorithm = content[pos++] == 'a'
            if (isAlgorithm) {
                val mode = readAlgorithm(content, dictionary.idDictionary)
                dictionary.idMode = settingsRepository.saveModeSettings(mode.toMode())
            } else {
                --pos
            }
            val words = readWords(content, dictionary, isAlgorithm)
            curentIxAddWord = 0
            addWords(words, dictionary, showPremiumDialog)
        }
        navigatorV2.toast(R.string.import_success)
    }

    private suspend fun addWords(
        words: List<Word>,
        dictionary: Dictionary,
        showPremiumDialog: suspend (text: String, advertisementWasViewed: suspend () -> Unit) -> Unit
    ) {
        while (curentIxAddWord < words.size) {
            if (isStarted.not() && currentWord + curentIxAddWord >= limitWords) {
                var text = navigatorV2.getString(R.string.suggestion_of_additional_words_s_d_d)
                text = String.format(
                    text,
                    addNumberFreeWords,
                    dictionary.name,
                    curentIxAddWord,
                    words.size
                )
                premiumRepository.saveCurrentLimitWords(currentWord + curentIxAddWord)
                showPremiumDialog.invoke(text) {
                    limitWords += addNumberFreeWords
                    premiumRepository.saveCurrentLimitWords(limitWords)
                }
            } else {
                val word = words[curentIxAddWord++]
                word.idWord = dictionaryRepository.addWord(word)
                word.notifications?.forEach { item ->
                    item.idWord = word.idWord
                    item.idMode = dictionary.idMode
                    dictionaryRepository.saveNotificationHistoryItem(item)
                }
            }
        }
        premiumRepository.saveCurrentLimitWords(limitWords)
    }

    private fun readAlgorithm(str: String, idDictionary: Long): ModeDbEntity {
        val selectedMode = readWord(str)
        val sampleDays = readWord(str).toBoolean()
        val daysInJson = readWord(str)
        val timeIntervals = readWord(str).toBoolean()
        val timeIntervalsFirst = readWord(str)
        val timeIntervalsSecond = readWord(str)
        return ModeDbEntity(
            idMode = 0,
            idDictionary = idDictionary,
            selectedMode = selectedMode,
            sampleDays = sampleDays,
            daysInJson = daysInJson,
            timeIntervals = timeIntervals,
            timeIntervalsFirst = timeIntervalsFirst,
            timeIntervalsSecond = timeIntervalsSecond
        )
    }

    private fun readWords(str: String, dictionary: Dictionary, isAlgorithm: Boolean): List<Word> {
        val wordList = mutableListOf<Word>()
        while (pos < str.length && str[pos++] == 'w') {
            val textFirst = readWord(str)
            val textLast = readWord(str)
            if (isAlgorithm) {
                val word = readFullDataWord(str, dictionary.idDictionary, textFirst, textLast)
                if (str[pos] == 'h') {
                    word.notifications = readHistoryNotification(str, dictionary.idMode)
                }
                wordList.add(word)
            } else {
                wordList.add(Word(dictionary.idDictionary, textFirst, textLast))
            }
        }
        --pos
        return wordList
    }

    private fun readHistoryNotification(str: String, idMode: Long): List<NotificationHistoryItem> {
        val notifications = mutableListOf<NotificationHistoryItem>()
        while (pos < str.length && str[pos++] == 'h') {
            val dateMention = readWord(str).toLong()
            val learnStep = readWord(str).toInt()
            notifications.add(
                NotificationHistoryItem(
                    idWord = -1,
                    dateMention = dateMention,
                    idMode = idMode,
                    learnStep = learnStep
                )
            )
        }
        --pos
        return notifications
    }

    private fun readFullDataWord(
        str: String,
        idDictionary: Long,
        textFirst: String,
        textLast: String
    ): Word {
        val allNotificationsCreated = readWord(str).toBoolean()
        val learnStep = readWord(str).toInt()
        val lastDateMention = readWord(str).toLong()
        val uniqueId = readWord(str).toInt()
        return Word(
            idDictionary = idDictionary,
            textFirst = textFirst,
            textLast = textLast,
            allNotificationsCreated = allNotificationsCreated,
            learnStep = learnStep,
            lastDateMention = lastDateMention,
            uniqueId = uniqueId
        )
    }

    private fun readDictionary(str: String): Dictionary {
        if (str[pos++] != '{') throw IOException()
        var name = readWord(str)
        val dateCreated = readWord(str).toLong()
        val idFolder = readWord(str).toLong()
        val include = readWord(str).toBoolean()

        name = name.ifBlank { navigatorV2.getString(R.string.dictionary) }
        while (currentDictionariesNames.contains(name)) {
            name += " (new)"
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