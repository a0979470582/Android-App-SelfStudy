package com.bu.selfstudy.ui.book

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.LinearLayoutManager
import com.bu.selfstudy.R
import com.bu.selfstudy.databinding.FragmentBookBinding
import com.bu.selfstudy.showToast
import com.bu.selfstudy.ui.word.WordAdapter
import com.bu.selfstudy.ui.word.WordFragmentArgs

class BookFragment : Fragment() {

    private var _binding : FragmentBookBinding? = null
    private val binding get() = _binding!!

    //等待登入功能製作, 才需要參數
    //private val args: BookFragmentArgs by navArgs()
    private val viewModel:BookViewModel by viewModels()

    private val adapter =  BookAdapter()
    private lateinit var tracker: SelectionTracker<Long>
    private var searchView: SearchView? = null
    private var actionMode: ActionMode? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //swipeRefresh
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)//三色輪流
        binding.swipeRefresh.setOnRefreshListener {loadBooks()}

        //recyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = adapter



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