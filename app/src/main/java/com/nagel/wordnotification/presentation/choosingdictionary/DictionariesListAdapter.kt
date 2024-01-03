package com.nagel.wordnotification.presentation.choosingdictionary

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.nagel.wordnotification.R
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

class DictionariesListAdapter(
    private var dictionaries: Flow<List<Dictionary>>,
    private var settingsRepository: SettingsRepository,
    private val context: Context,
    private val selectDictionary: (Long) -> Unit,
    private val showMenuActionOnWord: (dictionary: Dictionary, position: Int) -> Unit,
    private val setActive: (dictionary: Dictionary, active: Boolean) -> Unit,
    private val openModeSettings: (idDictionary: Long) -> Unit
) : RecyclerView.Adapter<DictionariesListAdapter.Holder>() {

    private var size: Int = 0
    private var dataList = listOf<Dictionary>()

    init {
        dictionaries.onEach { list ->
            size = list.size
            val diffCallback = ArticleDiffItemCallback(dataList, list)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            dataList = ArrayList(list).map {
                it.wordList = ArrayList(it.wordList)
                it
            }
            diffResult.dispatchUpdatesTo(this)
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
        val currentBackground =
            (R.drawable.background_card_dictionary_1 + currentDictionary.idDictionary % 7).toInt()
        holder.binding.apply {
            backgroundCard.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    currentBackground
                )
            )
            val currentLearnedWords = currentDictionary.wordList.count { it.isItWasRepeated() }
            name.text = currentDictionary.name
            data.text = generateStringData(currentDictionary.wordList)
            progress.text = "${currentLearnedWords}/${currentDictionary.wordList.size}"

            root.setOnClickListener {
                selectDictionary.invoke(currentDictionary.idDictionary)
            }
            root.setOnLongClickListener {
                showMenuActionOnWord.invoke(currentDictionary, position)
                true
            }
            isActive.isChecked = currentDictionary.include
            notificationIcon.isVisible = !isActive.isChecked
            notificationIconActive.isVisible = isActive.isChecked

            isActive.setOnClickListener {
                notificationIcon.isVisible = !isActive.isChecked
                notificationIconActive.isVisible = isActive.isChecked
                setActive.invoke(currentDictionary, isActive.isChecked)
                CoroutineScope(Dispatchers.IO).launch {
                    val mode = settingsRepository.getModeSettingsById(currentDictionary.idMode)
                    if (mode == null) {
                        openModeSettings.invoke(currentDictionary.idDictionary)
                    }
                }
            }
        }
    }

    private fun generateStringData(listWord: List<Word>): String {
        var resultString = ""
        repeat(listWord.size) {
            resultString += listWord[it].textFirst + ", "
        }
        return resultString.removeSuffix(", ")
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

    private class ArticleDiffItemCallback(
        val oldList: List<Dictionary>,
        val newList: List<Dictionary>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldList.size - oldItemPosition - 1].idDictionary ==
                    newList[newList.size - newItemPosition - 1].idDictionary
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldList.size - oldItemPosition - 1] ==
                    newList[newList.size - newItemPosition - 1]
        }
    }
}