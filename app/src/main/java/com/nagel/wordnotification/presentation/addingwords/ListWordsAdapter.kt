package com.nagel.wordnotification.presentation.addingwords

import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word


class ListWordsAdapter(
    dictionary: Dictionary,
    private val showActionMenuWithView: (Word, position: Int) -> Unit
) : RecyclerView.Adapter<ListWordsAdapter.Holder>() {

    private val wordList = dictionary.wordList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val idLayout = when (viewType) {
            WORD_TYPE_1 -> R.layout.item_word_type_1
            WORD_TYPE_2 -> R.layout.item_word_type_2
            WORD_TYPE_3 -> R.layout.item_word_type_3
            else -> R.layout.item_word_type_1
        }
        val view = LayoutInflater.from(parent.context).inflate(idLayout, parent, false)

        return Holder(view)
    }

    override fun getItemCount(): Int {
        return wordList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        Log.d("ListWordsAdapter: bind", position.toString())
        val currentWord = wordList[wordList.size - position - 1]
        holder.setWordFirst(currentWord.textFirst)
        holder.setWordSecond(currentWord.textLast)
        holder.view.tag = currentWord
        holder.view.setOnLongClickListener {
            val actualWord = wordList.find { currentWord.hashCode() == it.hashCode() }
            actualWord?.let { showActionMenuWithView.invoke(it, holder.adapterPosition) }
            true
        }
    }



    override fun getItemViewType(position: Int): Int {
        val words = wordList[position]
        return when {
            words.textFirst.length <= 11 && words.textLast.length <= 11 -> WORD_TYPE_1
            words.textFirst.length <= 20 && words.textLast.length <= 50 -> WORD_TYPE_2
            else -> WORD_TYPE_3
        }
    }

    class Holder(
        val view: View
    ) : RecyclerView.ViewHolder(view) {

        private val firstText = view.findViewById<TextView>(R.id.first_word)
        private val lastText = view.findViewById<TextView>(R.id.last_word)

        fun setWordFirst(text: String) {
            firstText.text = text
        }

        fun setWordSecond(text: String) {
            lastText.text = text
        }

    }

    class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) : ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            if (parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount - 1) {
                outRect.bottom = verticalSpaceHeight;
            }
        }
    }

    companion object {
        private const val WORD_TYPE_1 = 1
        private const val WORD_TYPE_2 = 2
        private const val WORD_TYPE_3 = 3

        private const val COUNT_LINE_LETTER = 18
    }
}