package com.bu.selfstudy.ui.word

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.bu.selfstudy.R
import com.bu.selfstudy.databinding.ActivityWordBinding
import com.bu.selfstudy.logic.model.Word
import com.bu.selfstudy.showToast


class WordActivity : AppCompatActivity(){
    private val viewModel by lazy { ViewModelProvider(this).get(WordViewModel::class.java) }

    private var _binding : ActivityWordBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WordAdapter
    private lateinit var tracker: SelectionTracker<Long>
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = WordAdapter(viewModel.wordList)


        viewModel.apply { intent.apply {
            bookId = getLongExtra("book_id", 0)
            bookName = getStringExtra("book_name")?:"預設題庫"
        } }

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
                loadWordsByBookId()
            }
        }

        //recyclerView
        binding.recyclerView.let {
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = adapter
            it.setHasFixedSize(true)
        }

        //selection tracker
        tracker = SelectionTracker.Builder(
            "word-recycler-view",
            binding.recyclerView,
            IdItemKeyProvider(adapter),
            IdItemDetailsLookup(binding.recyclerView),
            StorageStrategy.createLongStorage()
        ).build()

        tracker.addObserver(selectionObserver)
        adapter.tracker = tracker

        viewModel.selection?.let {
            if(it.size()>0){
                tracker.setItemsSelected(it, true)
            }
        }


        //main
        loadWordsByBookId()
        viewModel.wordListLiveData.observe(this, Observer { result ->
            val words = result.getOrNull()
            when{
                words!=null && viewModel.searchQuery.isNotBlank() ->{
                    "必須根據query值來過濾wordList".showToast()
                    //搜尋相關的功能, TODO
                }
                words!=null-> {
                    viewModel.wordList.clear()
                    viewModel.wordList.addAll(words)
                    adapter.notifyDataSetChanged()
                    "已刷新成功".showToast()
                }
                else-> {
                    "沒有獲取word".showToast()
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
            binding.swipeRefresh.isRefreshing = false
        })
    }

    //搜尋相關的功能, TODO
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.word_toolbar, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                /**調用RecyclerView內的Filter方法 */
                Log.e("onQuery", query)
                when {
                    query.isBlank() and viewModel.wordList2.isEmpty() -> {
                        return false
                    }
                    query.isBlank() -> {
                        viewModel.wordList.clear()
                        viewModel.wordList.addAll(viewModel.wordList2)
                    }
                    else -> {
                        viewModel.wordList.clear()
                        viewModel.wordList.addAll(viewModel.wordList2.filter {
                            it.wordName.contains(query, true)
                        } as ArrayList<Word>)
                    }
                }
                adapter.notifyDataSetChanged()
                return false
            }
        })
        searchView.setOnSearchClickListener(View.OnClickListener {
            viewModel.wordList2.clear()
            viewModel.wordList2.addAll(viewModel.wordList)
        })
        searchView.setOnCloseListener {
            viewModel.wordList2.clear()
            true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    private val selectionObserver = object : SelectionTracker.SelectionObserver<Long>() {
        override fun onSelectionChanged() {
            super.onSelectionChanged()
            tracker?.let {
                if (!it.hasSelection()) {
                    actionMode?.finish()
                } else {
                    if (actionMode == null)
                        actionMode = startSupportActionMode(actionModeCallback)
                    actionMode?.title = "${it.selection.size()}"
                }
            }
        }
    }
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.action_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item?.itemId) {
                else->false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            tracker?.clearSelection()
            actionMode = null
        }
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        tracker.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        tracker.onSaveInstanceState(outState)
    }

    private fun loadWordsByBookId(){
        viewModel.loadWordsByBookId()
        binding.swipeRefresh.isRefreshing = true
    }
}