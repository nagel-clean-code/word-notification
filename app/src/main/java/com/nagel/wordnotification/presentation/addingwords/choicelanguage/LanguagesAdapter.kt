/*
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package com.nagel.wordnotification.presentation.addingwords.choicelanguage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.nagel.wordnotification.R

class LanguagesAdapter(
    private val currentSelected: String?,
    private val listItem: List<String>,
    private val selected: (Int) -> Unit
) : RecyclerView.Adapter<LanguagesAdapter.Holder>() {

    private val radioGroup = mutableSetOf<RadioButton>()
    private var selectedPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.language_item, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.apply {
            val button = view.findViewById<RadioButton>(R.id.button)
            val name = listItem[position]
            button.text = name

            if (selectedPosition != null) {
                button.isChecked = selectedPosition == position
            } else {
                button.isChecked = name == currentSelected
            }

            radioGroup.add(button)
            button.setOnClickListener {
                selectedPosition = position
                selected.invoke(position)
                radioGroup.forEach {
                    if (it != button) {
                        it.isChecked = false
                    }
                }
            }
        }
    }

    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItemCount(): Int = listItem.size

    inner class Holder(val view: View) : ViewHolder(view)
}
