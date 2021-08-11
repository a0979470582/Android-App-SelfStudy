package com.bu.selfstudy.ui.recentword

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bu.selfstudy.NavGraphDirections
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.RecentWord
import com.bu.selfstudy.databinding.FragmentMarkBinding
import com.bu.selfstudy.databinding.FragmentRecentWordBinding
import com.bu.selfstudy.tool.*


class RecentWordFragment : Fragment() {
    private val viewModel: RecentWordViewModel by viewModels()
    private val binding: FragmentRecentWordBinding by viewBinding()
    private val listAdapter = RecentWordAdapter(fragment = this)


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {

        binding.recyclerView.let {
            it.adapter = listAdapter
            it.setHasFixedSize(true)
        }

        return binding.root
    }


     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         setHasOptionsMenu(true)

         viewModel.recentWordLiveData.observe(viewLifecycleOwner){
             binding.recentNotFound.root.isVisible = it.isEmpty()

             listAdapter.submitList(it)
         }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_search -> {
                findNavController().navigate(R.id.searchFragment)
            }
            R.id.action_add_book -> {
                findNavController().navigate(R.id.addBookFragment)

            }
            R.id.action_download_book -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.book_toolbar, menu)
    }



    fun navigateWordCardFragment(recentWord: RecentWord) {
        findNavController().navigate(
                NavGraphDirections.actionGlobalWordFragment(
                        bookId = recentWord.bookId,
                        wordId = recentWord.wordId
                )
        )
    }

    fun refreshRecentWord(recentWord: RecentWord) {
        viewModel.refreshRecentWord(recentWord)
    }
}

