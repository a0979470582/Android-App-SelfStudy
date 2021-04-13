package com.bu.selfstudy.ui.book

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.FragmentBookBinding
import com.bu.selfstudy.tools.*

class BookFragment : Fragment() {
    //等待登入功能製作, 才需要參數
    //private val args: BookFragmentArgs by navArgs()
    private val viewModel:BookViewModel by viewModels()
    private val binding : FragmentBookBinding by viewBinding()

    private val adapter =  BookAdapter()
    private lateinit var tracker: SelectionTracker<Long>
    private var searchView: SearchView? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle? ):View{

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
    }
}
/*
        binding.recyclerView.let {
            it.adapter = this.adapter
            it.setHasFixedSize(true)
        }

        tracker = buildSelectionTracker(
                binding.recyclerView,
                viewModel.idList,
                SelectionPredicates.createSelectSingleAnything(),
                R.menu.book_action_mode,
                ::onActionItemClicked
        ).also { adapter.tracker = it }

        //FAB
        binding.fab.setOnClickListener {
            navigateToAddWordFragment()
        }


        //main
        viewModel.bookListLD.observe(viewLifecycleOwner){ books ->
            adapter.setBookList(books)
            viewModel.setIdList(books)
            viewModel.isLoadingLD.value = false
            "已獲取${books.size}筆資料".showToast()
        }

        getDatabaseResult(viewModel.databaseEventLD){_, message, _ ->
            message.showToast()
        }

        getDialogResult()

    }



    private fun navigateToAddWordFragment() {
        val action = BookFragmentDirections.actionGlobalAddWordFragment(
                0L,
                viewModel.bookListLD.value!!.map { it.bookName }.toTypedArray(),
                viewModel.bookListLD.value!!.map { it.id }.toLongArray()
        )
        findNavController().navigate(action)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_search->{
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.book_toolbar, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        tracker.onRestoreInstanceState(savedInstanceState)
    }

    private fun onActionItemClicked(itemId:Int){
        when (itemId) {
            R.id.action_delete -> {
                val action = BookFragmentDirections.actionGlobalToDeleteDialog(
                        "是否刪除 ${tracker.selection.size()} 個題庫 ?")
                findNavController().navigate(action)
            }
        }
    }

    private fun getDialogResult() {
        getDialogResult {dialogName, bundle ->
            when(dialogName){
                "AddWordFragment"->{
                    bundle.getParcelable<Word>("word").let { word->
                        viewModel.insertWords(listOfNotNull(word))
                    }
                }
            }
        }
    }
}*/