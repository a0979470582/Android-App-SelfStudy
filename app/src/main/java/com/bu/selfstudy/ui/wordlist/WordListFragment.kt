package com.bu.selfstudy.ui.wordlist

import androidx.appcompat.view.ActionMode
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.WordTuple
import com.bu.selfstudy.databinding.FragmentWordListBinding
import com.bu.selfstudy.databinding.WordListItemBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.tool.myselectiontracker.IdItemDetailsLookup
import com.bu.selfstudy.tool.myselectiontracker.IdItemKeyProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch


class WordListFragment : Fragment() {
    private val listViewModel: WordListViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val binding: FragmentWordListBinding by viewBinding()
    private val listAdapter = WordListAdapter(listFragment = this)

    private var searchView: SearchView? = null
    private var actionMode: ActionMode? = null

    private lateinit var tracker: SelectionTracker<Long>

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {

        binding.recyclerView.let {
            it.adapter = listAdapter
            it.setHasFixedSize(true)
        }
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }


     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         setHasOptionsMenu(true)
         lifecycleScope.launch {
             initSelectionTracker()
             binding.fastScroller.attachFastScrollerToRecyclerView(binding.recyclerView)
         }

        activityViewModel.currentOpenBookLiveData.observe(viewLifecycleOwner) {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = it.bookName
            listViewModel.currentOpenBookLiveData.value = it
        }

         listViewModel.wordListLiveData.observe(viewLifecycleOwner){
             listAdapter.submitList(it)
             refreshWordIdList(it)
         }

         listViewModel.updateEvent.observe(this){
             "已儲存成功".showToast()
         }

         listViewModel.markEvent.observe(this){
             when(it){
                 "mark" -> resources.getString(R.string.toast_success_mark).showToast()
                 "cancel_mark" -> resources.getString(R.string.toast_cancel_mark).showToast()
             }
         }

         listViewModel.insertEvent.observe(this){
             binding.root.showSnackbar("已新增了${it!!.size}個單字", "檢視"){
                 "正在檢視中...".showToast()
             }
         }

         listViewModel.deleteEvent.observe(this){
             "已移除${it}個單字".showToast()
         }
         listViewModel.deleteToTrashEvent.observe(this){
             binding.root.showSnackbar("已將${it}個單字移至回收桶", "回復"){
                 "正在回復中...".showToast()
             }
         }


    }

    fun refreshWordIdList(wordList: List<WordTuple>){
        listViewModel.refreshWordIdList(wordList)
    }

    private fun initSelectionTracker() {
        tracker = SelectionTracker.Builder(
                "recycler-view-word-fragment",
                binding.recyclerView,
                IdItemKeyProvider(listViewModel.wordIdList),
                IdItemDetailsLookup(binding.recyclerView, listViewModel.wordIdList),
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything())
                .build().also {
                    listAdapter.tracker = it
                }

        val selectionObserver = object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()

                if (!tracker.hasSelection()) {
                    actionMode?.finish()
                } else {
                    lifecycleScope.launch {
                        activityViewModel.bookListLiveData.value?.let {
                            listViewModel.refreshLongPressedWord(tracker.selection.toList())
                        }
                    }
                    if (actionMode == null) {
                        actionMode = (activity as AppCompatActivity)
                                .startSupportActionMode(actionModeCallback)
                    }
                    actionMode?.title = "${tracker.selection.size()}/${listViewModel.wordListLiveData.value?.size}"
                }
            }
        }

        tracker.addObserver(selectionObserver)
    }



    fun updateMarkWord(wordId:Long, isMark: Boolean){
        listViewModel.updateMarkWord(wordId, isMark)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_search -> {
            }
            R.id.action_filter->{
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.wordlist_toolbar, menu)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        tracker.onRestoreInstanceState(savedInstanceState)
    }

    val actionModeCallback = object : androidx.appcompat.view.ActionMode.Callback {
        /**對於搜尋結果進行操作, 會將ActionMode疊在SearchView上方, 此時鍵盤不會消失
        造成使用者能在看不到搜尋框時查詢, 因此ActionMode出現就必須關閉鍵盤
         */
        override fun onCreateActionMode(mode: androidx.appcompat.view.ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.wordlist_action_mode, menu)
            closeKeyboard()
            return true
        }

        override fun onPrepareActionMode(mode: androidx.appcompat.view.ActionMode, menu: Menu) = false

        override fun onActionItemClicked(mode: androidx.appcompat.view.ActionMode, item: MenuItem): Boolean {
            actionModeMenuCallback(item.itemId)
            return true
        }

        override fun onDestroyActionMode(mode: androidx.appcompat.view.ActionMode) {
            tracker.clearSelection()
            actionMode = null
        }
    }

    private fun actionModeMenuCallback(itemId:Int){
        when (itemId) {
            R.id.action_delete -> {
                MaterialAlertDialogBuilder(requireActivity())
                        .setTitle("刪除")
                        .setMessage("是否刪除 ?")
                        .setPositiveButton("確定") { dialog, which ->
                            listViewModel.deleteWordToTrash(0)
                            //activityViewModel.deleteWordToTrash(args.wordId)
                            //setNavigationResult("isDelete", true)
                        }
                        .setNegativeButton("取消") { dialog, which ->
                        }
                        .create()
            }
            R.id.action_choose_all -> {
                //viewModel.idList.let {
                    //tracker.setItemsSelected(it,it.size != tracker.selection.size()
                    //)
                //}
            }

            R.id.action_copy -> {
                //navigateToChooseBookDialog("copy")
            }
            R.id.action_move -> {
                //navigateToChooseBookDialog("move")
                /*viewModel.longPressedBook?.let {
                    val action = BookFragmentDirections.actionBookFragmentToEditBookDialog(it.bookName)
                    findNavController().navigate(action)
                }*/
            }
        }
    }
}

/*



    /**遺珠之憾:1. 空query時旋轉螢幕無法保留介面*/
    /**在這裡產生searchView對象*/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.word_toolbar, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val searchManager = requireActivity().getSystemService(SEARCH_SERVICE) as SearchManager
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

        searchView?.apply{
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                /**避免按下送出後, 鍵盤消失, 但輸入框的光標仍在閃爍*/
                override fun onQueryTextSubmit(query: String): Boolean {
                    clearFocus()
                    return false
                }

                /**每一次搜尋文字改變, 就觸發資料庫刷新*/
                override fun onQueryTextChange(query: String): Boolean {
                    viewModel.searchQueryLD.value = query
                    return false
                }
            })

            /**如果頁面重建, ViewModel保有查詢字串, 表示先前頁面銷毀時, 使用者正在使用查詢
            我們知道頁面銷毀會使SearchView也銷毀, 但SearchView在第一次開啟或最後銷毀時, 都會
            觸發onQueryTextChange且query是空值, 注意expandActionView就會觸發此情形*/
            val pendingQuery = viewModel.searchQueryLD.value
            if(pendingQuery!=null && pendingQuery.isNotBlank()){
                searchItem.expandActionView()
                setQuery(pendingQuery, false)
                clearFocus()
            }
        }
    }



}*/

