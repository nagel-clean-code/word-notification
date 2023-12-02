package com.nagel.wordnotification.presentation.randomizer

import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.presentation.base.BaseViewModel
import com.nagel.wordnotification.presentation.randomizer.RandomizingFragment.Companion.EMPTY_WORD
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RandomizingVM @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val sessionRepository: SessionRepository
) : BaseViewModel() {

    val showResult = MutableStateFlow<Pair<Int, Int>?>(null)
    val currentDictionary = MutableStateFlow<String?>(null)
    val currentWord = MutableStateFlow<Word?>(null)
    val loadingDictionaries = MutableStateFlow<List<Dictionary>?>(null)
    val listWord = mutableListOf<Word>()
    private val listIndexes1 = mutableListOf<Int>()
    val selectedDictionarySet = mutableSetOf<String>()
    var countRemember = 0
    var countNotRemember = 0

    init {
        viewModelScope.launch {
            val session = sessionRepository.getSession()
            session?.account?.id?.let { id ->
                dictionaryRepository.loadDictionaries(id).collect() { dictionaries ->
                    dictionaries.forEach {
                        selectedDictionarySet.add(it.name)
                    }
                    loadingDictionaries.value = dictionaries
                    initWords()
                }
            }
        }
    }

    private fun initWords() {
        countRemember = 0
        countNotRemember = 0
        listIndexes1.clear()
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

    fun selectedDictionary(name: String) {
        selectedDictionarySet.add(name)
        initWords()
    }

    fun unselectedDictionary(name: String) {
        selectedDictionarySet.remove(name)
        initWords()
    }

    fun notRemember() {
        if (!isFinish()) {
            ++countNotRemember
        }
    }

    fun remember() {
        if (!isFinish()) {
            ++countRemember
        }
    }

    private fun isFinish() = (listIndexes1.isEmpty() && (countRemember > 0 || countNotRemember > 0))

    fun nextWord() {
        if (listWord.isEmpty()) {
            showWord(Word(0, EMPTY_WORD, EMPTY_WORD))
            return
        }
        if (isFinish()) {
            val result = Pair(countRemember, listWord.size)
            countRemember = 0
            countNotRemember = 0
            showResult.value = result
            showResult.value = null
        }
        if (listIndexes1.isEmpty()) {
            initList1()
        }
        val randomIx = (0 until listIndexes1.size).random()
        val wordIx = listIndexes1[randomIx]
        showWord(listWord[wordIx])
        listIndexes1.removeAt(randomIx)
    }

    private fun showWord(word: Word) {
        currentWord.value = word
        val dictionary = loadingDictionaries.value?.find { it.idDictionary == word.idDictionary }
        currentDictionary.value = dictionary?.name ?: ""
    }
}