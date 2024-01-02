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

package com.nagel.wordnotification.presentation.choosingdictionary.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary

class ExpListAdapter(
    private val list: List<Dictionary>,
    private val checkedChange: (Dictionary, Boolean) -> Unit
) : BaseExpandableListAdapter() {

    private val checkedList = mutableSetOf<AppCompatCheckBox?>()
    private var groupMapWithIx = mutableMapOf<Int, Dictionary?>()

    init {
        list.map { it.name }.toSet().forEachIndexed { ix, nameGroup ->
            val wordsGroup = list.firstOrNull { it.name == nameGroup }
            groupMapWithIx[ix] = wordsGroup
        }
    }

    fun clearSelected() {
        checkedList.forEach {
            it?.isChecked = false
        }
        checkedList.clear()
    }

    override fun getGroupCount(): Int {
        return groupMapWithIx.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return groupMapWithIx[groupPosition]?.wordList?.size ?: 0
    }

    override fun getGroup(groupPosition: Int): Any? {
        return groupMapWithIx[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return groupMapWithIx[groupPosition]!!.wordList[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean, convertView: View?,
        parent: ViewGroup
    ): View {
        var newConvertView = convertView

        if (newConvertView == null) {
            newConvertView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_library_dictionary, parent, false)
        }

        val icon = newConvertView?.findViewById(R.id.arrow) as? ImageView
        if (isExpanded) {
            icon?.setImageResource(R.drawable.baseline_arrow_drop_down_24)
        } else {
            icon?.setImageResource(R.drawable.baseline_arrow_drop_up_24)
        }
        val dictionary = groupMapWithIx[groupPosition]!!
        val name = dictionary.name
        val childTextView = newConvertView?.findViewById(R.id.nameGroup) as? TextView
        childTextView?.text = name

        val isCheckedGroup = newConvertView?.findViewById(R.id.isCheckedGroup) as? AppCompatCheckBox
        checkedList.add(isCheckedGroup)
        isCheckedGroup?.setOnCheckedChangeListener { _, checked ->
            checkedChange.invoke(dictionary, checked)
        }
        return newConvertView!!
    }

    override fun getChildView(
        groupPosition: Int, childPosition: Int, isLastChild: Boolean,
        convertView: View?, parent: ViewGroup
    ): View {
        var newConvertView = convertView
        if (newConvertView == null) {
            newConvertView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_library_word, parent, false)
        }

        val currentWord = groupMapWithIx[groupPosition]!!.wordList[childPosition]
        val childTextView = newConvertView!!.findViewById(R.id.word) as TextView
        val textWord = currentWord.textFirst + " - " + currentWord.textLast
        childTextView.text = textWord

        if (childPosition == groupMapWithIx[groupPosition]!!.wordList.size - 1) {
            childTextView.setBackgroundResource(R.drawable.last_background_library_item)
        } else {
            childTextView.setBackgroundColor(parent.context.getColor(R.color.light_beige))
        }
        return newConvertView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}
