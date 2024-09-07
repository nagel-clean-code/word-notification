package com.nagel.wordnotification.presentation.onboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.nagel.wordnotification.R

class OnBoardAdapter : RecyclerView.Adapter<PagerVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH =
        PagerVH(LayoutInflater.from(parent.context).inflate(R.layout.onboard_page, parent, false))

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: PagerVH, position: Int) = holder.itemView.run {
        holder.setImageView(images[position])
    }

    companion object {
        val images = intArrayOf(
            R.drawable.preview_add_word_1,
            R.drawable.preview_add_word_2,
            R.drawable.preview_add_word_3,
            R.drawable.preview_mode_1,
            R.drawable.preview_dictionary_1
        )
    }
}

class PagerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val imageView = itemView.findViewById<AppCompatImageView>(R.id.imageView)

    fun setImageView(id: Int) {
        imageView.setImageResource(id)
    }

}