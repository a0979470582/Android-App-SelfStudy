package com.bu.selfstudy.ui.word

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bu.selfstudy.R
import com.bu.selfstudy.databinding.ActivityWordBinding
import com.bu.selfstudy.databinding.FragmentBookBinding
import com.bu.selfstudy.databinding.WordItemBinding
import com.bu.selfstudy.showToast
import com.bu.selfstudy.ui.book.BookAdapter
import com.bu.selfstudy.ui.book.BookViewModel

class WordActivity : AppCompatActivity() {
    private val viewModel by lazy { ViewModelProvider(this).get(WordViewModel::class.java) }
    //private val viewModel = ViewModelProvider(this).get(WordViewModel::class.java)

    private var _binding : ActivityWordBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.bookId = intent.getLongExtra("book_id", 0)
        viewModel.bookName = intent.getStringExtra("book_name")?:"預設題庫"

        //Toolbar
        binding.toolbar.title = viewModel.bookName
        binding.toolbar.inflateMenu(R.menu.toolbar)
        binding.toolbar.setNavigationIcon(R.drawable.baseline_account_box_24)
        binding.toolbar.setOnMenuItemClickListener{true}


        //swipeRefresh
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)//三色輪流
        binding.swipeRefresh.setOnRefreshListener {loadWordsByBookId()}

        //recyclerView
        adapter = WordAdapter(viewModel.wordList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter


        //main
        loadWordsByBookId()
        viewModel.wordListLiveData.observe(this,  Observer{ result ->
            val words = result.getOrNull()
            if (words != null) {
                viewModel.wordList.clear()
                viewModel.wordList.addAll(words)
                adapter.notifyDataSetChanged()
                "已刷新成功".showToast()
            } else {
                "沒有獲取word".showToast()
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.swipeRefresh.isRefreshing = false
        })
    }

    private fun loadWordsByBookId(){
        viewModel.loadWordsByBookId()
        binding.swipeRefresh.isRefreshing = true
    }
}