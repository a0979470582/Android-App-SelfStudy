package com.bu.selfstudy.ui.word

import android.app.SearchManager
import android.content.Context
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.bu.selfstudy.R
import com.bu.selfstudy.databinding.ActivityWordBinding
import com.bu.selfstudy.logic.model.Word
import com.bu.selfstudy.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class WordActivity : AppCompatActivity(){

    private var _binding : ActivityWordBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel:WordViewModel
    private lateinit var adapter: WordAdapter
    private lateinit var tracker: SelectionTracker<Long>
    private var searchView: SearchView? = null
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleIntent(intent)

        viewModel = ViewModelProvider(this,
            WordViewModelFactory(
                    intent.getLongExtra("book_id", 0),
                    intent.getStringExtra("book_name")?: "",
                    this,
                    savedInstanceState
            )
        ).get(WordViewModel::class.java)

        adapter = WordAdapter()

        //Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply{
            setDisplayHomeAsUpEnabled(true)
            title = viewModel.bookName
        }

        //swipeRefresh
        binding.swipeRefresh.apply {
            setColorSchemeResources(R.color.colorPrimary)
            setOnRefreshListener {
                viewModel.searchQuery.apply {
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
        viewModel.wordList.observe(this){ words ->
            adapter.setWordList(words)
            adapter.notifyDataSetChanged()
            binding.swipeRefresh.isRefreshing = false
            "已獲取${words.size}筆資料".showToast()
        }
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
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.word_toolbar, menu)
        val menuItem = menu.findItem(R.id.action_search)
        searchView = menuItem.actionView as SearchView

        //顯示出語音輸入
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView?.let {
            it.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            it.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean{
                    it.clearFocus()
                    return false
                }

                override fun onQueryTextChange(query: String): Boolean {
                    viewModel.searchQuery.value = query
                    return false
                }
            })

            it.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(view: View?) {
                }

                override fun onViewDetachedFromWindow(view: View?) {
                }
            })

            val pendingQuery = viewModel.searchQuery.value
            if(pendingQuery!=null && pendingQuery.isNotBlank()){
                menuItem.expandActionView()
                it.setQuery(pendingQuery, false)
                it.clearFocus()
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
                    "${tracker.selection.size()}/${viewModel.wordList.value!!.size}"
            }
        }
    }

    //set actionMode, for multiple selection
    private val actionModeCallback = object : ActionMode.Callback {
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
                                binding.swipeRefresh.isRefreshing = true
                                lifecycleScope.launch(Dispatchers.Default) {
                                    viewModel.deleteWords(tracker.selection.toList())
                                }
                                tracker.clearSelection()
                            }
                            .setNegativeButton("取消"){dialog, which->

                            }
                            .create()
                            .show()
                }
                R.id.action_choose_all -> {
                }
                R.id.action_copy -> {
                }
                R.id.action_move -> {
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
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }
    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                searchView?.setQuery(query, false)
            }
        }
    }
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