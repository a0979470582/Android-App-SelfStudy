package com.bu.selfstudy.tool.myselectiontracker

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.tool.log

class IdItemDetailsLookup(
    private val recyclerView: RecyclerView,
    private val idList: List<Long>,
    private val hasHeader: Boolean
) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            recyclerView.getChildViewHolder(view).adapterPosition.let {
                if(it > idList.size)
                    return null

                return if(hasHeader && it!=0)
                    IdItemDetails(idList[it-1], it)
                else
                    IdItemDetails(idList[it], it)
            }
        }
        return null
    }
}