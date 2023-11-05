package com.nagel.wordnotification.presentation.choosingdictionary

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.databinding.ItemCardDictionaryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.Integer.min

class DictionariesListAdapter(
    private val dictionaryRepository: DictionaryRepository,
    private var settingsRepository: SettingsRepository,
    private val allWord: List<Word>,
    private val idAccount: Long,
    private val context: Context,
    private val selectDictionary: (Long) -> Unit,
    private val showMenuActionOnWord: (dictionary: Dictionary, position: Int) -> Unit,
    private val setActive: (dictionary: Long, active: Boolean) -> Unit,
    private val openModeSettings: (idDictionary: Long) -> Unit
) : RecyclerView.Adapter<DictionariesListAdapter.Holder>() {

    var dictionaries: Flow<List<Dictionary>> = dictionaryRepository.loadDictionaries(idAccount)
    private var size: Int = 0
    private var dataList = listOf<Dictionary>()

    init {
        dictionaries.onEach {
            size = it.size
            dataList = it
            notifyDataSetChanged()
        }.launchIn(MainScope())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view =
            ItemCardDictionaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val currentDictionary = dataList[dataList.size - 1 - position]
        currentDictionary.wordList =
            allWord.filter {
                it.idDictionary == currentDictionary.idDictionaries
            }.toMutableList()

        val currentBackground =
            (R.drawable.background_card_dictionary_1 + currentDictionary.idDictionaries % 7).toInt()
        holder.binding.apply {
            backgroundCard.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    currentBackground
                )
            )
            val currentLearnedWords = currentDictionary.wordList.count { it.learned }
            name.text = currentDictionary.name
            data.text = generateStringData(currentDictionary.wordList)
            progress.text = "${currentLearnedWords}/${currentDictionary.wordList.size}"

            root.setOnClickListener {
                selectDictionary.invoke(currentDictionary.idDictionaries)
            }
            root.setOnLongClickListener {
                showMenuActionOnWord.invoke(currentDictionary, position)
                true
            }
            isActive.isChecked = currentDictionary.include

            isActive.setOnClickListener {
                setActive.invoke(currentDictionary.idDictionaries, isActive.isChecked)
                CoroutineScope(Dispatchers.IO).launch {
                    val mode = settingsRepository.getModeSettings(currentDictionary.idDictionaries)
                    if (mode == null) {
                        openModeSettings.invoke(currentDictionary.idDictionaries)
                    }
                }
            }

        }
    }

    private fun generateStringData(listWord: List<Word>): String {
        var resultString = ""
        repeat(min(6, listWord.size)) {
            resultString += listWord[it].textFirst + ", "
        }
        resultString.removeSuffix(",")
        return resultString
    }

    class Holder(
        val binding: ItemCardDictionaryBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) : ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1) {
                outRect.bottom = verticalSpaceHeight
            }
        }
    }
}