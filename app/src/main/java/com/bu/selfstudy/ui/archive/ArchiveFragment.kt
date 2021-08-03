package com.bu.selfstudy.ui.archive

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.NavGraphDirections
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.databinding.FragmentBookBinding
import com.bu.selfstudy.tool.*
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class ArchiveFragment : Fragment() {
    private val viewModel: ArchiveViewModel by viewModels()
    private val binding : FragmentBookBinding by viewBinding()
    private val adapter = ArchiveAdapter(this)

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?):View{

        binding.recyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        viewModel.bookListLiveData.observe(viewLifecycleOwner){
            adapter.submitList(it)

        }

        lifecycleScope.launch {
            setDialogResultListener()
            setDatabaseListener()
        }


    }


    fun navigateToWordCardFragment(bookId: Long){
        findNavController().navigate(
                NavGraphDirections.actionGlobalWordCardFragment(bookId = bookId)
        )
    }

    private fun setDatabaseListener() {
        viewModel.databaseEvent.observe(viewLifecycleOwner){
            when(it?.first){
                "delete" -> "已刪除「${it.second?.getString("bookName")}」".showToast()
                "archive" -> "已取消封存「${it.second?.getString("bookName")}」".showToast()
                "update" -> "更新成功".showToast()
                "insertBook" -> "新增成功".showToast()
                "insertLocal" -> "已新增「${it.second?.getString("bookName")}」".showToast()
            }
        }
    }

    private fun setDialogResultListener() {
        setFragmentResultListener("DialogDeleteCommon") { _, _ ->
            viewModel.deleteBook()
        }
        setFragmentResultListener("DialogEditBook"){ _, bundle->
            val newBookName = bundle.getString("bookName")!!
            //val newBookExplanation = bundle.getString("explanation")!!
            viewModel.editBook(newBookName)
        }
        setFragmentResultListener("DialogAddBook"){ _, bundle->
            val newBookName = bundle.getString("bookName")!!
            viewModel.insertBook(newBookName)
        }
        setFragmentResultListener("DialogArchiveBook"){ _, bundle->
            viewModel.archiveBook(false)
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

    fun setChosenBook(book: Book) {
        viewModel.chosenBook = book
    }

    fun updateBookColor(colorInt: Int){
        viewModel.updateBookColor(colorInt)
    }
    fun archiveBook(isArchive: Boolean){
        viewModel.archiveBook(isArchive)
    }

}