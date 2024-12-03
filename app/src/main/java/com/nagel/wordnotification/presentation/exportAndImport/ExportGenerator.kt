package com.nagel.wordnotification.presentation.exportAndImport

import com.nagel.wordnotification.app.App
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.settings.SettingsRepository
import java.io.File
import java.io.PrintWriter
import javax.inject.Inject

class ExportGenerator @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val settingsRepository: SettingsRepository
) {

    private var fileCash: File? = null

    fun writeOneDictionary(
        dictionary: Dictionary
    ): File {
        val file = createFile("${dictionary.name}$FILE_FORMAT_TXT")
        file.printWriter().use { out ->
            dictionary.wordList.forEach { word ->
                word.apply {
                    out.print("[$textFirst;$textLast]")
                }
            }
        }
        return file
    }

    suspend fun writeDictionaries(
        dictionaries: List<Dictionary>,
        isAlgorithm: Boolean
    ): File {
        val file = createFile("dictionaries$FILE_FORMAT_FIRE")
        file.printWriter().use { out ->
            dictionaries.forEach() { dictionary ->
                out.print("{|${dictionary.name}|}")
                val mode = settingsRepository.getModeSettingsById(dictionary.idMode)
                mode?.apply {
                    out.print("a|$selectedMode||$sampleDays||$daysInJson||$timeIntervals|$timeIntervalsFirst|$timeIntervalsSecond|")
                }
                dictionary.wordList.forEach { word ->
                    word.apply {
                        out.print("w|$$textFirst||$textLast|")
                        if (isAlgorithm) {
                            writeAlgorithm(out, word, dictionary.idMode)
                        }
                    }
                }
            }
        }
        return file
    }

    private suspend fun writeAlgorithm(out: PrintWriter, word: Word, idMode: Long) = with(word) {
        out.print("|$allNotificationsCreated||$learnStep||$lastDateMention||$uniqueId|")
        val historyList = dictionaryRepository.loadHistoryNotification(idWord, idMode)
        historyList?.forEach { item ->
            out.print("h|${item.dateMention}||${item.learnStep}|")
        }
    }

    private fun createFile(name: String): File {
        val file = File(App.get().filesDir, name)
        fileCash = file
        return file
    }

    fun deleteLastFile() {
        fileCash?.delete()
    }

    companion object {
        const val FILE_FORMAT_TXT = ".txt"
        const val FILE_FORMAT_FIRE = ".fire"
    }
}