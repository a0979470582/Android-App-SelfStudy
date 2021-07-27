package com.bu.selfstudy.ui.book

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.databinding.FragmentBookBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.tool.myselectiontracker.IdItemDetailsLookup
import com.bu.selfstudy.tool.myselectiontracker.IdItemKeyProvider
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class BookFragment : Fragment() {

    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val viewModel: BookViewModel by viewModels()
    private val binding : FragmentBookBinding by viewBinding()
    private val adapter = BookAdapter(this)

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?):View{

        binding.recyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        activityViewModel.bookListLiveData.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }

        binding.firstRow.setOnClickListener {
            val action = BookFragmentDirections.actionBookFragmentToRecentWordFragment()
            findNavController().navigate(action)
        }

        lifecycleScope.launch {
            initSpeedDial()
            setDialogResultListener()
            setDatabaseListener()
        }
    }
    fun navigateToWordCardFragment(bookId: Long){
        val action = BookFragmentDirections
                .actionBookFragmentToWordCardFragment(bookId = bookId)
        findNavController().navigate(action)
    }

    private fun setDatabaseListener() {
        viewModel.databaseEvent.observe(viewLifecycleOwner){
            when(it?.first){
                "delete" -> "已刪除「${it.second?.getString("bookName")}」".showToast()
                "archive" -> "已封存「${it.second?.getString("bookName")}」".showToast()
                "update" -> "更新成功".showToast()
                "insertBook" -> "新增成功".showToast()
                "insertLocal" -> "已新增「${it.second?.getString("bookName")}」".showToast()
            }
        }
    }

    private fun setDialogResultListener() {
        setFragmentResultListener("DialogDeleteCommon") { _, _ ->
            viewModel.longPressedBook?.let {
                viewModel.deleteBook(it.id, it.bookName)
            }
        }
        setFragmentResultListener("DialogEditBook"){ _, bundle->
            viewModel.longPressedBook?.copy()?.let {
                it.bookName = bundle.getString("bookName")!!
                viewModel.updateBook(it)
            }
        }
        setFragmentResultListener("DialogAddBook"){ _, bundle->
            val bookName = bundle.getString("bookName")!!
            viewModel.insertBook(bookName)
        }
        setFragmentResultListener("DialogArchiveBook"){ _, bundle->
            viewModel.longPressedBook?.let {
                viewModel.archiveBook(it.id, it.bookName)
            }
        }
    }

    private fun initSpeedDial() {
        if(binding.speedDialView.actionItems.isNotEmpty())
            return

        with(binding.speedDialView){
            this.mainFab.setOnLongClickListener {
                resources.getString(R.string.FAB_main).showToast()
                return@setOnLongClickListener true
            }

            this.addAllActionItems(listOf(
                    ActionItemCreator.addWordItem,
                    ActionItemCreator.addBookItem)
            )

            // Set option fabs click listeners.
            this.setOnActionSelectedListener { actionItem ->
                when (actionItem.id) {
                    R.id.book_fragment_fab_add_word -> {
                        findNavController().navigate(R.id.addWordFragment)
                    }
                    R.id.book_fragment_fab_add_book -> {
                        val action = BookFragmentDirections
                                .actionBookFragmentToAddBookDialog()
                        findNavController().navigate(action)
                    }
                }
                return@setOnActionSelectedListener false //關閉小按鈕
            }

        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        var backPressedToExitOnce: Boolean = false

        requireActivity().onBackPressedDispatcher.addCallback(this){
            if(binding.speedDialView.isOpen){
                binding.speedDialView.close()
                return@addCallback
            }

            if (backPressedToExitOnce) {
                requireActivity().finish()
                return@addCallback
            }

            backPressedToExitOnce = true
            resources.getString(R.string.toast_exit_app).showToast()
            lifecycleScope.launch {
                delay(2000)
                backPressedToExitOnce = false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.calculateBookSize()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_search -> {
                findNavController().navigate(R.id.searchFragment)
            }
            R.id.action_add_book -> {
                val action = BookFragmentDirections.actionBookFragmentToAddBookDialog()
                findNavController().navigate(action)
            }
            R.id.action_test -> {

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

    fun setLongPressedBook(book: Book) {
        viewModel.longPressedBook = book
    }

}