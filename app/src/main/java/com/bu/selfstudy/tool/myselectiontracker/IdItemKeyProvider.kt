package com.bu.selfstudy.tool.myselectiontracker

import androidx.recyclerview.selection.ItemKeyProvider
import com.bu.selfstudy.tool.log

class IdItemKeyProvider(
    private val idList: List<Long>,
    private val hasHeader: Boolean
) : ItemKeyProvider<Long>(SCOPE_CACHED) {
    override fun getKey(position: Int): Long{
        if(position > idList.size)
            return 0

        return if(hasHeader && position!=0)
            idList[position-1]
        else
            idList[position]
    }
    override fun getPosition(key: Long): Int {
        val index = idList.indexOf(key)

        return if (hasHeader) {
            if (index == -1) -1 else index + 1
        } else {
            index
        }
    }
}
