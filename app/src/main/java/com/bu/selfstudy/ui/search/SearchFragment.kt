package com.bu.selfstudy.ui.search

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.bu.selfstudy.R
import com.bu.selfstudy.databinding.FragmentBookBinding
import com.bu.selfstudy.databinding.FragmentSearchBinding
import com.bu.selfstudy.tool.viewBinding

class SearchFragment: Fragment()  {
    private val binding : FragmentSearchBinding by viewBinding()
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle? ):View{

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_toolbar, menu)

        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        searchView.setQuery("f", false)
    }
}