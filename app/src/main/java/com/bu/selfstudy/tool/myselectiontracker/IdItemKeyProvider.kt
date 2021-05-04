package com.bu.selfstudy.tool.myselectiontracker

import androidx.recyclerview.selection.ItemKeyProvider

class IdItemKeyProvider(private val idList: List<Long>) : ItemKeyProvider<Long>(SCOPE_CACHED) {
    override fun getKey(position: Int): Long = idList[position]
    override fun getPosition(key: Long): Int = idList.indexOf(key)
}
