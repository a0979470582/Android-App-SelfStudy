package com.bu.selfstudy.ui.word

import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.MutableSelection
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.bu.selfstudy.R
import com.bu.selfstudy.databinding.ActivityWordBinding
import com.bu.selfstudy.showToast


class WordActivity : AppCompatActivity(){

    private var _binding : ActivityWordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WordViewModel by viewModels {
        WordViewModel.provideFactory(
                intent.getLongExtra("book_id", 0),
                intent.getStringExtra("book_name")?: "",
        )
    }

    private lateinit var adapter: WordAdapter
    private lateinit var tracker: SelectionTracker<Long>
    private var searchView: SearchView? = null
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = WordAdapter()

        //Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply{
            setDisplayHomeAsUpEnabled(true)
            title = viewModel.bookName
        }

        /**swipeRefresh
         重設自身來觸發資料庫刷新
         */
        binding.swipeRefresh.apply {
            setColorSchemeResources(R.color.colorPrimary)
            setOnRefreshListener {
                viewModel.searchQueryLD.apply {
                    value = value
                }
            }
        }

        //recyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@WordActivity)
            adapter = this@WordActivity.adapter
            setHasFixedSize(true)
        }

        //selection tracker
        tracker = SelectionTracker.Builder(
                "word-recycler-view",
                binding.recyclerView,
                IdItemKeyProvider(adapter),
                IdItemDetailsLookup(binding.recyclerView),
                StorageStrategy.createLongStorage()
        ).build().also {
            adapter.tracker = it
            it.addObserver(selectionObserver)
        }

        //main
        /**
         監聽資料庫刷新, 並通知adapter
         */
        viewModel.wordListLD.observe(this){ words ->
            adapter.setWordList(words)
            adapter.notifyDataSetChanged()
            binding.swipeRefresh.isRefreshing = false
            "已獲取${words.size}筆資料".showToast()
        }
        viewModel.bookListLD.observe(this){}
    }

    //toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    //搜尋相關的功能, SearchView
    //遺珠之憾:1. 空query時旋轉螢幕無法保留介面
    /**
     在這裡產生searchView對象
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.word_toolbar, menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menuItem.actionView as SearchView

        searchView?.apply {
            //開啟語音查詢
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                //避免按下送出後, 鍵盤消失, 但輸入框的光標仍在閃爍
                override fun onQueryTextSubmit(query: String): Boolean{
                    clearFocus()
                    return false
                }

                //每一次搜尋文字改變, 就觸發資料庫刷新
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
                menuItem.expandActionView()
                setQuery(pendingQuery, false)
                clearFocus()
            }
        }
        return true
    }

    //set actionMode, for multiple selection
    private val selectionObserver = object : SelectionTracker.SelectionObserver<Long>() {
        override fun onSelectionChanged() {
            super.onSelectionChanged()
            if (!tracker.hasSelection()) {
                actionMode?.finish()
            } else {
                if (actionMode == null)
                    actionMode = startSupportActionMode(actionModeCallback)
                actionMode?.title =
                    "${tracker.selection.size()}/${viewModel.wordListLD.value!!.size}"
            }
        }
    }

    //set actionMode, for multiple selection
    private val actionModeCallback = object : ActionMode.Callback {
        //對於搜尋結果進行操作, 會將ActionMode疊在SearchView上方, 此時鍵盤不會消失
        //造成使用者能在看不到搜尋框時查詢, 因此ActionMode出現就必須關閉鍵盤
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            closeKeyboard()
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.action_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item?.itemId) {
                R.id.action_delete -> {
                    AlertDialog.Builder(this@WordActivity)
                            .setTitle("刪除")
                            .setMessage("是否刪除 ${tracker.selection.size()} 個單字 ?")
                            .setPositiveButton("確定"){dialog, which->
                                viewModel.deleteWords(tracker.selection.toList())
                                tracker.clearSelection()
                                binding.swipeRefresh.isRefreshing = true
                            }
                            .setNegativeButton("取消"){dialog, which->}
                            .create()
                            .show()
                }
                R.id.action_choose_all -> {
                    viewModel.wordListLD.value?.let {wordList->
                        val wordIdOfAll:List<Long> = wordList.map {it.id}
                            tracker.setItemsSelected(
                                    wordIdOfAll,
                                    wordList.size != tracker.selection.size()
                            )
                    }
                }
                R.id.action_copy -> {
                    viewModel.bookListLD.value?.let { bookList ->
                        val bookListToShow = bookList.filter { it.id != viewModel.bookId }
                        val bookNameToShow = bookListToShow.map{ it.bookName }
                        var position = 0

                        AlertDialog.Builder(this@WordActivity)
                            .setTitle("複製到題庫")
                            .setSingleChoiceItems(bookNameToShow.toTypedArray(), position) { _, which ->
                                position = which
                            }
                            .setPositiveButton("確定") {_,_->
                                viewModel.copyWordsTo(
                                        tracker.selection.toList(),
                                        bookListToShow[position].id
                                )
                                tracker.clearSelection()
                                binding.swipeRefresh.isRefreshing = true
                            }
                            .setNegativeButton("取消") {_,_->}
                            .create()
                            .show()

                    }
                }
                R.id.action_move -> {
                    viewModel.bookListLD.value?.let { bookList ->
                        val bookListToShow = bookList.filter { it.id != viewModel.bookId }
                        val bookNameToShow = bookListToShow.map{ it.bookName }
                        var position = 0

                        AlertDialog.Builder(this@WordActivity)
                                .setTitle("轉移到題庫")
                                .setSingleChoiceItems(bookNameToShow.toTypedArray(), position) { _, which ->
                                    position = which
                                }
                                .setPositiveButton("確定") {_,_->
                                    viewModel.moveWordsTo(
                                            tracker.selection.toList(),
                                            bookListToShow[position].id
                                    )
                                    tracker.clearSelection()
                                    binding.swipeRefresh.isRefreshing = true
                                }
                                .setNegativeButton("取消") {_,_->}
                                .create()
                                .show()

                    }
                }
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            tracker?.clearSelection()
            actionMode = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker.onSaveInstanceState(outState)

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        tracker.onRestoreInstanceState(savedInstanceState)
    }

    private fun deleteWords(){
        binding.swipeRefresh.isRefreshing = true
    }
    private fun copyWordsTo(query: String){
        binding.swipeRefresh.isRefreshing = true
    }
    private fun moveWordsTo(query: String){
        binding.swipeRefresh.isRefreshing = true
    }
    /*搜尋功能是監聽query值來載入資料庫, 因此不必按下送出即可進行查詢, 但使用者按下送出或是採用語音查詢時,
     會觸發activity的singleTop, 此時activity生命週期不會執行, 只會觸發onNewIntent, 但會發現搜尋框是空值,
     因此這裡呼叫setQuery
    */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                searchView?.setQuery(query, false)
            }
        }
    }

    //關閉鍵盤
    private fun closeKeyboard(){
        if(currentFocus!=null){
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(
                        currentFocus?.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                )
        }
    }
}