package com.bu.selfstudy.ui.word

import androidx.recyclerview.selection.ItemKeyProvider

class IdItemKeyProvider(private val adapter: WordAdapter) : ItemKeyProvider<Long>(SCOPE_CACHED) {
    override fun getKey(position: Int): Long = adapter.getItemId(position)
    override fun getPosition(key: Long): Int = adapter.getPosition(key)
}