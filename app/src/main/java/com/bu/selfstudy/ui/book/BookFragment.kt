package com.bu.selfstudy.ui.book

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.SearchRepository
import com.bu.selfstudy.databinding.FragmentBookBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.tool.myselectiontracker.IdItemDetailsLookup
import com.bu.selfstudy.tool.myselectiontracker.IdItemKeyProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class BookFragment : Fragment() {

    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val viewModel: BookViewModel by viewModels()
    private val binding : FragmentBookBinding by viewBinding()
    private val adapter = BookListAdapter(this)

    private lateinit var tracker: SelectionTracker<Long>

    private var searchView: SearchView? = null
    private var actionMode: ActionMode? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?):View{

        binding.recyclerView.let {
            it.adapter = this.adapter
            it.setHasFixedSize(true)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        lifecycleScope.launch {
            //loadLocalBook()
            //SearchRepository.insertLocalAutoComplete()
            setDialogResultListener()
            initSelectionTracker()
            //SearchRepository.removeLocalAutoComplete()
            //SearchRepository.clearSearchHistory()
        }

        activityViewModel.bookListLiveData.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }

        viewModel.databaseEvent.observe(viewLifecycleOwner){
            when(it?.first){
                "delete" -> "已刪除「${it.second?.getString("bookName")}」".showToast()
                "update" -> "更新成功".showToast()
                "insertBook" -> "新增成功".showToast()
                "insertLocal" -> "已新增「${it.second?.getString("bookName")}」".showToast()
            }
        }
        binding.fab.setOnClickListener {
            actionMode?.finish()
            findNavController().navigate(R.id.addWordFragment)
        }
    }


    private fun loadLocalBook(){
        lifecycleScope.launch {
            val bookNameList = listOf(
                    "toeicData.json" to "多益高頻單字",
                    "ieltsData.json" to "雅思核心單字",
                    "commonly_use_1000.json" to "最常用1000字",
                    "commonly_use_3000.json" to "最常用3000字",
                    "high_school_level_1.json" to "高中英文分級Level1",
                    "high_school_level_2.json" to "高中英文分級Level2",
                    "high_school_level_3.json" to "高中英文分級Level3",
                    "high_school_level_4.json" to "高中英文分級Level4",
                    "high_school_level_5.json" to "高中英文分級Level5",
                    "high_school_level_6.json" to "高中英文分級Level6",
                    "junior_school_basic_1200.json" to "國中基礎英文1200字",
                    "junior_school_difficult_800.json" to "國中進階英文800字",
                    "elementary_school_basic_word.json" to "小學基礎單字"
            )
            bookNameList.forEach {
                BookRepository.insertLocalBook(it.first)
            }

        }
    }

    fun navigateToWordCardFragment(book: Book){
        activityViewModel.currentOpenBookLiveData.value = book
        findNavController().navigate(R.id.wordCardFragment)
    }

    private fun setDialogResultListener() {
        setFragmentResultListener("delete") { _, _ ->
            viewModel.longPressedBook?.let {
                viewModel.deleteBook(it.id, it.bookName)
            }
        }
        setFragmentResultListener("edit"){ _, bundle->
            viewModel.longPressedBook?.copy()?.let {
                it.bookName = bundle.getString("bookName")!!
                viewModel.updateBook(it)
            }
        }
        setFragmentResultListener("insertBook"){ _, bundle->
            val bookName = bundle.getString("bookName")!!
            viewModel.insertBook(bookName)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        var backPressedToExitOnce: Boolean = false

        requireActivity().onBackPressedDispatcher.addCallback(this){
            if (backPressedToExitOnce) {
                requireActivity().finish()
                return@addCallback
            }

            backPressedToExitOnce = true
            resources.getString(R.string.toast_exit_app).showToast()
            lifecycleScope.launch {
                delay(2000)
                backPressedToExitOnce = false
            }
        }
    }


    private fun initSelectionTracker() {
        if(::tracker.isInitialized)
            return

        tracker = SelectionTracker.Builder(
                "recycler-view-book-fragment",
                binding.recyclerView,
                IdItemKeyProvider(activityViewModel.bookIdList),
                IdItemDetailsLookup(binding.recyclerView, activityViewModel.bookIdList),
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectSingleAnything())
         .build().also {
             adapter.tracker = it
         }

        val selectionObserver = object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                if (!tracker.hasSelection()) {
                    actionMode?.finish()
                } else {
                    activityViewModel.bookListLiveData.value?.let {
                        viewModel.refreshLongPressedBook(it, tracker.selection.toList()[0])
                    }
                    if (actionMode == null) {
                        actionMode = (activity as AppCompatActivity)
                                .startSupportActionMode(actionModeCallback)
                    }
                }
            }
        }

        tracker.addObserver(selectionObserver)
    }

    //set actionMode, for multiple selection
    val actionModeCallback = object : androidx.appcompat.view.ActionMode.Callback {
        /**對於搜尋結果進行操作, 會將ActionMode疊在SearchView上方, 此時鍵盤不會消失
        造成使用者能在看不到搜尋框時查詢, 因此ActionMode出現就必須關閉鍵盤
         */
        override fun onCreateActionMode(mode: androidx.appcompat.view.ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.book_action_mode, menu)
            closeKeyboard()
            return true
        }

        override fun onPrepareActionMode(mode: androidx.appcompat.view.ActionMode, menu: Menu) = false

        override fun onActionItemClicked(mode: androidx.appcompat.view.ActionMode, item: MenuItem): Boolean {
            actionModeMenuCallback(item?.itemId)
            return true
        }

        override fun onDestroyActionMode(mode: androidx.appcompat.view.ActionMode) {
            tracker.clearSelection()
            actionMode = null
        }
    }

    private fun actionModeMenuCallback(itemId: Int){
        when (itemId) {
            R.id.action_delete -> {
                viewModel.longPressedBook?.let {
                    val title = "刪除題庫"
                    val message = "刪除「${it.bookName}」?"
                    val action = BookFragmentDirections.actionGlobalDialogDeleteCommon(title, message)
                    findNavController().navigate(action)
                }
            }
            R.id.action_edit -> {
                viewModel.longPressedBook?.let {
                    val action = BookFragmentDirections.actionBookFragmentToEditBookDialog(it.bookName)
                    findNavController().navigate(action)
                }
            }
            R.id.action_archive -> {
                viewModel.longPressedBook?.let {
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_search -> {
                findNavController().navigate(R.id.searchFragment)
                //FragmentNavigatorExtras()
            }
            R.id.action_add_book -> {
                val action = BookFragmentDirections.actionBookFragmentToAddBookDialog()
                findNavController().navigate(action)
            }
            R.id.action_test -> {

            }
            R.id.action_download_book -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.book_toolbar, menu)
        searchView = menu.findItem(R.id.action_search).actionView as SearchView



        searchView?.apply{
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                /**避免按下送出後, 鍵盤消失, 但輸入框的光標仍在閃爍*/
                override fun onQueryTextSubmit(query: String): Boolean {
                    clearFocus()
                    return true
                }

                /**每一次搜尋文字改變, 就觸發資料庫刷新*/
                override fun onQueryTextChange(query: String): Boolean {
                    //viewModel.searchQueryLD.value = query

                    return true
                }
            })

            /**如果頁面重建, ViewModel保有查詢字串, 表示先前頁面銷毀時, 使用者正在使用查詢
            我們知道頁面銷毀會使SearchView也銷毀, 但SearchView在第一次開啟或最後銷毀時, 都會
            觸發onQueryTextChange且query是空值, 注意expandActionView就會觸發此情形*/
            /*val pendingQuery = viewModel.searchQueryLD.value
            if(pendingQuery!=null && pendingQuery.isNotBlank()){
                searchItem.expandActionView()
                setQuery(pendingQuery, false)
                clearFocus()
            }*/
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(::tracker.isInitialized)
            tracker.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if(::tracker.isInitialized)
            tracker.onRestoreInstanceState(savedInstanceState)
    }
}