package com.nagel.wordnotification.presentation.profile

import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.core.algorithms.Algorithm
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.data.settings.room.RoomModeRepository
import com.nagel.wordnotification.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileVM @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val sessionRepository: SessionRepository,
    private val modeRepository: RoomModeRepository,
) : BaseViewModel() {

    val showData = MutableStateFlow<Boolean?>(null)
    private lateinit var modes: List<ModeSettingsDto>

    var numbersWords: Int = 0
    var countDictionaries: Int = 0
    var learnedWords: Int = 0
    var learnedDictionaries: Int = 0
    var remainingTimeToMemorize: Long = 0L


    init {
        viewModelScope.launch {
            val session = sessionRepository.getSession()
            session?.account?.id?.let { id ->
                val modesTmp = modeRepository.getModes()
                if (modesTmp != null) {
                    modes = modesTmp
                } else {
                    return@let
                }
                dictionaryRepository.loadDictionariesFlow(id).collect() { dictionaries ->
                    startingAnalysis(dictionaries)
                }
            }
        }
    }

    private fun startingAnalysis(dictionaries: List<Dictionary>) {
        val listWords = mutableListOf<Word>()
        var maxTime = 0L
        dictionaries.forEach() lit@{ dictionary ->
            val mode = modes.find { it.idMode == dictionary.mode }?.selectedMode ?: return@lit

            val modeNumbersSteps = mode.getCountSteps()

            val words = dictionary.wordList
            val currentLearnedWords = words.filter { it.learnStep >= modeNumbersSteps }.size
            if (currentLearnedWords == dictionary.wordList.size) {
                learnedDictionaries++
            }
            learnedWords += currentLearnedWords

            listWords.addAll(words)

            if(dictionary.include) {
                words.forEach {
                    val time = getRemainingTime(it, mode)
                    if (maxTime < time) {
                        maxTime = time
                    }
                }
            }
        }
        countDictionaries = dictionaries.size
        numbersWords = listWords.size
        remainingTimeToMemorize = maxTime
        showData.value = true
    }

    private fun getRemainingTime(word: Word, mode: Algorithm): Long {
        var time = 0L
        for (i in word.learnStep + 1..mode.getCountSteps()) {
            time = mode.getNewDate(i, time) ?: 0
        }
        return time
    }
}