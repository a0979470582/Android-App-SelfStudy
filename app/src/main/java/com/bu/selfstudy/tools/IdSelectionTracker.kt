package com.bu.selfstudy.tools

import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.MainActivity
import com.bu.selfstudy.R
import com.bu.selfstudy.ui.word.WordAdapter
import com.bu.selfstudy.ui.word.WordFragmentDirections

class IdItemDetails(private val id: Long, private val _position: Int)
    : ItemDetailsLookup.ItemDetails<Long>() {
    override fun getPosition() = _position
    override fun getSelectionKey() = id
}

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

 class IdItemKeyProvider(private val idList: List<Long>) : ItemKeyProvider<Long>(SCOPE_CACHED) {
    override fun getKey(position: Int): Long = idList[position]
    override fun getPosition(key: Long): Int = idList.indexOf(key)
}


fun Fragment.buildSelectionTracker(
        recyclerView: RecyclerView,
        idList: List<Long>,
        predicate: SelectionTracker.SelectionPredicate<Long>,
        actionModeMenuRes: Int,
        onActionItemClicked: (itemId:Int)->Unit
): SelectionTracker<Long> {

    val activity = this.requireActivity()
    var actionMode = (activity as MainActivity).actionMode

    val tracker = SelectionTracker.Builder(
            "recyclerview${(this.javaClass).name}",
            recyclerView,
            IdItemKeyProvider(idList),
            IdItemDetailsLookup(recyclerView, idList),
            StorageStrategy.createLongStorage()
    ).withSelectionPredicate(predicate).build()

    //set actionMode, for multiple selection
    val actionModeCallback = object : androidx.appcompat.view.ActionMode.Callback {
        //對於搜尋結果進行操作, 會將ActionMode疊在SearchView上方, 此時鍵盤不會消失
        //造成使用者能在看不到搜尋框時查詢, 因此ActionMode出現就必須關閉鍵盤
        override fun onCreateActionMode(mode: androidx.appcompat.view.ActionMode, menu: Menu): Boolean {
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.word_action_mode, menu)
            closeKeyboard()//tools
            return true
        }

        override fun onPrepareActionMode(mode: androidx.appcompat.view.ActionMode, menu: Menu) = false

        override fun onActionItemClicked(mode: androidx.appcompat.view.ActionMode, item: MenuItem): Boolean {
            onActionItemClicked(item?.itemId)
            return true
        }

        override fun onDestroyActionMode(mode: androidx.appcompat.view.ActionMode) {
            tracker?.clearSelection()
            actionMode = null
        }
    }
    val selectionObserver = object : SelectionTracker.SelectionObserver<Long>() {
        override fun onSelectionChanged() {
            super.onSelectionChanged()
            if (!tracker.hasSelection()) {
                actionMode?.finish()
            } else {
                if (actionMode == null) {
                    actionMode = activity.startSupportActionMode(actionModeCallback)
                }
                actionMode?.title =
                        "${tracker.selection.size()}/${idList.size}"
            }
        }
    }
    return tracker.also { it.addObserver(selectionObserver)}
}
