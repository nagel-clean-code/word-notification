package com.nagel.wordnotification.presentation.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nagel.wordnotification.databinding.PremiumAdvantageItemBinding

class AdvantagesPremiumAdapter(
    private val list: List<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PremiumAdvantageItemBinding.inflate(inflater, parent, false)
        binding.root.tag = binding
        return object : RecyclerView.ViewHolder(binding.root) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = holder.itemView.tag as PremiumAdvantageItemBinding
        binding.name.text = list[position].replace("\\n", "\n")
    }

    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItemCount(): Int = list.size
}