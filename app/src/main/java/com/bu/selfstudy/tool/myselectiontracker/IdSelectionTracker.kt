package com.bu.selfstudy.tool.myselectiontracker

/*fun Fragment.buildIdSelectionTracker(
        recyclerView: RecyclerView,
        idList: List<Long>,
        actionModeMenuRes: Int,
        actionModeMenuCallback: (itemId:Int)->Unit,
        onCreateCallback: ((Unit)-> Unit)?=null,
        onDestroyCallback: ((Unit)-> Unit)?=null
        ): SelectionTracker<Long> {

    val activity = this.requireActivity()
    var actionMode = (activity as MainActivity).actionMode

    val tracker = SelectionTracker.Builder(
            "recyclerview${(this.javaClass).name}",
            recyclerView,
            IdItemKeyProvider(idList),
            IdItemDetailsLookup(recyclerView, idList),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything())
         .build()

    //set actionMode, for multiple selection
    val actionModeCallback = object : androidx.appcompat.view.ActionMode.Callback {
        /**對於搜尋結果進行操作, 會將ActionMode疊在SearchView上方, 此時鍵盤不會消失
           造成使用者能在看不到搜尋框時查詢, 因此ActionMode出現就必須關閉鍵盤
         */
        override fun onCreateActionMode(mode: androidx.appcompat.view.ActionMode, menu: Menu): Boolean {
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(actionModeMenuRes, menu)
            closeKeyboard()
            return true
        }

        override fun onPrepareActionMode(mode: androidx.appcompat.view.ActionMode, menu: Menu) = false

        override fun onActionItemClicked(mode: androidx.appcompat.view.ActionMode, item: MenuItem): Boolean {
            actionModeMenuCallback(item?.itemId)
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


    tracker.addObserver(selectionObserver)

    return tracker
}
*/