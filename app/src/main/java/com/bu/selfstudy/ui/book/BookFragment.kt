package com.bu.selfstudy.ui.book

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.FragmentBookBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.tool.myselectiontracker.buildIdSelectionTracker
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.data.model.Book

class BookFragment : Fragment() {

    private val activityViewModel: ActivityViewModel by activityViewModels()

    private val binding : FragmentBookBinding by viewBinding()

    private lateinit var adapter: BookAdapter
    private lateinit var tracker: SelectionTracker<Long>
    private var searchView: SearchView? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle? ):View{

        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        adapter = BookAdapter(this, activityViewModel.bookList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.setHasFixedSize(true)

        activityViewModel.bookListLiveData.observe(viewLifecycleOwner){
            adapter.notifyDataSetChanged()
        }

        tracker = buildIdSelectionTracker(
                binding.recyclerView,
                activityViewModel.bookList.map { it.id },
                R.menu.book_action_mode,
                ::actionModeMenuCallback
        ).also { adapter.tracker = it }
/*
        getDatabaseResult(viewModel.databaseEventLD){_, message, _ ->
            message.showToast()
        }

        getDialogResult()
        */

    }


    fun navigateToAddWordFragment() = findNavController().navigate(R.id.addWordFragment)


    fun navigateToWordFragment(book: Book){
        /*
        activityViewModel.bookLiveData.value = book
        val action = BookFragmentDirections.actionBookFragmentToWordFragment()
        //findNavController().navigate(R.id.wordFragment)
        findNavController().navigate(action)*/
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

        /*
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
        }*/
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //tracker.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        //tracker.onRestoreInstanceState(savedInstanceState)
    }

    private fun actionModeMenuCallback(itemId:Int){
        when (itemId) {
            /*
            R.id.action_delete -> {
                val message = "是否刪除 ${tracker.selection.size()} 個題庫 ?"
                val action = BookFragmentDirections.actionGlobalToDeleteDialog(message)
                findNavController().navigate(action)
            }*/
        }
    }
    /*
    private fun getDialogResult() {
        getDialogResult { dialogName, bundle ->
            when (dialogName) {
                "AddWordFragment" -> {
                    bundle.getParcelable<Word>("word").let { word ->
                        viewModel.insertWords(listOfNotNull(word))
                    }
                }
            }
        }
    }*/
}