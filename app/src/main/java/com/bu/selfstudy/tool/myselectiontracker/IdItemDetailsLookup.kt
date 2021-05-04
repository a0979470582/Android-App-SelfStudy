package com.bu.selfstudy.tool.myselectiontracker

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class IdItemDetailsLookup(
    private val recyclerView: RecyclerView,
    private val idList: List<Long>
) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            recyclerView.getChildViewHolder(view).adapterPosition.let {
                return IdItemDetails(idList[it], it)
            }
        }
        return null
    }
}