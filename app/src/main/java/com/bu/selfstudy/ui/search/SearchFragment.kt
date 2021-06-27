package com.bu.selfstudy.ui.search

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bu.selfstudy.R
import com.bu.selfstudy.databinding.FragmentSearchBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.ui.book.BookListAdapter
import com.bu.selfstudy.ui.book.BookViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SearchFragment: Fragment()  {
    private val binding : FragmentSearchBinding by viewBinding()
    private val adapter = SuggestionListAdapter(this)
    private val viewModel: SearchViewModel by viewModels()

    private lateinit var searchView: SearchView

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

        this@SearchFragment.refreshClipboardText()

        viewModel.searchQuery.value = ""//let's get data

        viewModel.suggestionList.observe(viewLifecycleOwner){
            lifecycleScope.launch { adapter.submitList(it) }
        }
    }

    private fun refreshClipboardText() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.refreshClipboardText(requireActivity().getClipboardText())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_toolbar, menu)

        searchView = menu.findItem(R.id.action_search).actionView as SearchView

        with(searchView){
            findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
                    ?.setImageDrawable(null)//the icon inside search box

            addOnLayoutChangeListener {_,_,_,_,_,_,_,_,_ ->
                (findViewById<SearchView.SearchAutoComplete>(R.id.search_src_text)
                        ?.layoutParams as LinearLayout.LayoutParams).leftMargin = -21
            }

            setIconifiedByDefault(false)
            requestFocus()
            openKeyboard()

            setSearchableInfo((requireActivity()
                    .getSystemService(Context.SEARCH_SERVICE) as SearchManager)
                    .getSearchableInfo(requireActivity().componentName))

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                /**避免按下送出後, 鍵盤消失, 但輸入框的光標仍在閃爍*/
                override fun onQueryTextSubmit(query: String): Boolean {
                    viewModel.addOneSearchHistory(query)
                    return false
                }

                /**每一次搜尋文字改變, 就觸發資料庫刷新*/
                override fun onQueryTextChange(query: String): Boolean {
                    if(query.isNullOrBlank())
                        this@SearchFragment.refreshClipboardText()
                    viewModel.searchQuery.value = query
                    return false
                }
            })
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                closeKeyboard()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun startSearch(suggestionName: String){
        searchView.setQuery(suggestionName, true)
    }
}