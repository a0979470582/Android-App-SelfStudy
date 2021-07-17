package com.bu.selfstudy.ui.recentword

import androidx.appcompat.view.ActionMode
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.RecentWord
import com.bu.selfstudy.data.model.WordTuple
import com.bu.selfstudy.databinding.FragmentWordListBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.tool.myselectiontracker.IdItemDetailsLookup
import com.bu.selfstudy.tool.myselectiontracker.IdItemKeyProvider
import kotlinx.coroutines.launch


class RecentWordFragment : Fragment() {
    private lateinit var viewModel: RecentWordViewModel
    private val binding: FragmentWordListBinding by viewBinding()
    private val listAdapter = RecentWordAdapter(fragment = this)

    private lateinit var tracker: SelectionTracker<Long>

    private var searchView: SearchView? = null
    private var actionMode: ActionMode? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {

        binding.recyclerView.let {
            it.adapter = listAdapter
            it.setHasFixedSize(true)
        }
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }


     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         setHasOptionsMenu(true)


         viewModel.wordListLiveData.observe(viewLifecycleOwner){
             //listAdapter.submitList(it)
             refreshWordIdList(it)
         }

         viewModel.databaseEvent.observe(viewLifecycleOwner){
             when(it?.first){
                 "delete"-> return@observe
                 "mark"->return@observe
                 "cancelMark"->return@observe
             }
         }

         lifecycleScope.launch {
             setDialogResultListener()
             //binding.fastScroller.attachFastScrollerToRecyclerView(binding.recyclerView)
         }
         //binding.fastScroller.touch
    }

    private fun setDialogResultListener() {
        setFragmentResultListener("delete") { _, _ ->
            viewModel.longPressedWordIdList.let {
            if(it.isNotEmpty())
                viewModel.deleteWordToTrash(it)
            }
        }
    }

    fun refreshWordIdList(wordList: List<WordTuple>){
        viewModel.refreshWordIdList(wordList)
    }



    fun updateMarkWord(wordId:Long, isMark: Boolean){
        viewModel.updateMarkWord(wordId, isMark)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_search -> {
            }
            R.id.action_filter->{
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.wordlist_toolbar, menu)
    }

    private fun actionModeMenuCallback(itemId:Int){
        when (itemId) {

        }
    }

    fun navigateWordCardFragment(recentWord: RecentWord) {
        val action = RecentWordFragmentDirections.actionRecentWordFragmentToWordCardFragment(
                bookId = recentWord.bookId,
                wordId = recentWord.wordId
        )
        findNavController().navigate(action)
    }
}

