package com.nagel.wordnotification.presentation.randomizer

import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.Constants.COUNT_FREE_USE_RANDOMIZER
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.premium.PremiumRepository
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.presentation.base.BaseViewModel
import com.nagel.wordnotification.presentation.randomizer.RandomizingFragment.Companion.EMPTY_WORD
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appmetrica.analytics.AppMetrica
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@HiltViewModel
class RandomizingVM @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val sessionRepository: SessionRepository,
    private val premiumRepository: PremiumRepository,
) : BaseViewModel() {

    val showResult = MutableStateFlow<Pair<Int, Int>?>(null)
    val currentDictionary = MutableStateFlow<String?>(null)
    val currentWord = MutableStateFlow<Word?>(null)
    val loadingDictionaries = MutableStateFlow<List<Dictionary>?>(null)
    val listWord = mutableListOf<Word>()
    private val listIndexes1 = mutableListOf<Int>()
    private val listPastIndexes = mutableListOf<Pair<Int, Boolean?>>()
    val selectedDictionarySet = mutableSetOf<String>()
    private var currentIx: Int = -1
    private var positionBack = -1
    private var isStarted = AtomicBoolean(false)
    private var limit = AtomicInteger(COUNT_FREE_USE_RANDOMIZER)
    var addNumberFreeRandomizer = AtomicInteger(COUNT_FREE_USE_RANDOMIZER)

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                isStarted.set(premiumRepository.getIsStarted())
                limit.set(premiumRepository.getCurrentLimitRandomizer())
                addNumberFreeRandomizer.set(premiumRepository.getAddNumberFreeRandomizer())
            }
            val session = sessionRepository.getSession()
            session.account?.id?.let { id ->
                dictionaryRepository.loadDictionariesFlow(id).collect() { dictionaries ->
                    dictionaries.forEach {
                        selectedDictionarySet.add(it.name)
                    }
                    loadingDictionaries.value = dictionaries
                    initWords()
                }
            }
        }
    }

    fun checkAvailableRandomizer(): Boolean {
        return isStarted.get() || limit.get() > 0
    }

    fun addFreeUse() {
        AppMetrica.reportEvent("reward_for_randomizer")
        viewModelScope.launch(Dispatchers.Default) {
            val updateLimit = addNumberFreeRandomizer.get()
            limit.set(updateLimit)
            premiumRepository.saveCurrentLimitRandomizer(updateLimit)
        }
    }

    private fun incrementFreeUseCount() {
        if (isStarted.get()) return
        val newLimit = limit.decrementAndGet()
        premiumRepository.saveCurrentLimitRandomizer(newLimit)
    }

    private fun initWords() {
        currentIx = -1
        positionBack = -1
        listIndexes1.clear()
        listPastIndexes.clear()
        listWord.clear()
        loadingDictionaries.value?.forEach {
            if (selectedDictionarySet.contains(it.name)) {
                listWord.addAll(it.wordList)
            }
        }
        nextWord()
    }

    private fun initList1() {
        for (i in 0 until listWord.size) {
            listIndexes1.add(i)
        }
    }

    fun getCountRemember() = listPastIndexes.count { it.second == true }

    fun getCountNotRemember() = listPastIndexes.count { it.second == false }

    fun getNumberOfMissed(): Int = listPastIndexes.filterIndexed { index, pair ->
        index < positionBack + 1
    }.count {
        it.second == null
    }

    fun selectedDictionary(name: String) {
        selectedDictionarySet.add(name)
        initWords()
    }

    fun unselectedDictionary(name: String) {
        selectedDictionarySet.remove(name)
        initWords()
    }

    fun notRemember() {
        if (listWord.isEmpty()) return
        if (!isFinish() || listPastIndexes.size - 1 < listWord.size) {
            updateAnswer(false)
        }
    }

    fun remember() {
        if (listWord.isEmpty()) return
        if (!isFinish() || listPastIndexes.size - 1 < listWord.size) {
            updateAnswer(true)
        }
    }

    private fun updateAnswer(isRemember: Boolean) {
        val new = listPastIndexes[positionBack].copy(second = isRemember)
        listPastIndexes[positionBack] = new
    }

    private fun isFinish(): Boolean {
        return getCountRemember() + getCountNotRemember() == listWord.size - getNumberOfMissed()
    }
//    private fun isFinish() = (listIndexes1.isEmpty() && (listPastIndexes.size > 0) && positionBack + 1 >= listPastIndexes.size) //TODO  переделать

    fun nextWord() {
        if (listWord.isEmpty()) {
            showWord(Word(0, EMPTY_WORD, EMPTY_WORD))
            return
        }
        if (isFinish()) {
            val result = Pair(getCountRemember(), listWord.size - getNumberOfMissed())
            showResult.value = result
            showResult.value = null
            listPastIndexes.clear()
            currentIx = -1
            positionBack = -1
            incrementFreeUseCount()
        }
        ++positionBack
        if (listIndexes1.isEmpty()) {
            initList1()
        }
        val randomIx = (0 until listIndexes1.size).random()
        val wordIx = listIndexes1[randomIx]

        currentIx = wordIx
        listPastIndexes.add(wordIx to null)
        showWord(listWord[wordIx])
        listIndexes1.removeAt(randomIx)
    }

    fun missWord() {
        if (positionBack + 1 >= listPastIndexes.size) {
            nextWord()
        } else {
            val pos = listPastIndexes[++positionBack].first
            showWord(listWord[pos])
        }
    }

    fun goBackPreviousWord() {
        if (listWord.isEmpty()) {
            showWord(Word(0, EMPTY_WORD, EMPTY_WORD))
            return
        }
        if (listPastIndexes.isEmpty() || positionBack <= 0) return
        val lastIx = listPastIndexes[--positionBack]
        showWord(listWord[lastIx.first])
    }

    private fun showWord(word: Word) {
        currentWord.value = word
        val dictionary = loadingDictionaries.value?.find { it.idDictionary == word.idDictionary }
        currentDictionary.value = dictionary?.name ?: ""
    }
}