package com.bu.selfstudy.ui.book

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
import com.bu.selfstudy.databinding.FragmentBookBinding
import com.bu.selfstudy.showToast

class BookFragment : Fragment() {
    private val viewModel by lazy { ViewModelProvider(this).get(BookViewModel::class.java) }

    private var _binding : FragmentBookBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: BookAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Toolbar
        binding.toolbar.title = "SelfStudy"
        binding.toolbar.inflateMenu(R.menu.book_toolbar)
        binding.toolbar.setNavigationIcon(R.drawable.round_menu_24)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.toolbar.setOnMenuItemClickListener{true}

        //nav
        binding.navView.setCheckedItem(R.id.nav_book)//設為默認選中
        binding.navView.setNavigationItemSelectedListener { //菜單點擊事件
            binding.drawerLayout.closeDrawers()
            true
        }

        //swipeRefresh
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)//三色輪流
        binding.swipeRefresh.setOnRefreshListener {loadBooks()}

        //recyclerView
        adapter = BookAdapter(this, viewModel.bookList)
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = adapter

        //FAB
        binding.fab.setOnClickListener {
            "FAB clicked".showToast()
        }

        //main
        loadBooks()
        viewModel.bookListLiveData.observe(viewLifecycleOwner,  Observer{ result ->
            if (result != null) {
                viewModel.bookList.clear()
                viewModel.bookList.addAll(result)
                adapter.notifyDataSetChanged()
                "已刷新成功".showToast()
            } else {
                "沒有獲取Book".showToast()
            }
            binding.swipeRefresh.isRefreshing = false
        })
    }

    private fun loadBooks(){
        viewModel.loadBooks()
        binding.swipeRefresh.isRefreshing = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}