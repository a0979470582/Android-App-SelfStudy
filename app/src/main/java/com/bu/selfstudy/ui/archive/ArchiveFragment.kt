package com.bu.selfstudy.ui.archive

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.bu.selfstudy.ui.main.MainActivity
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.databinding.FragmentArchiveBinding
import com.bu.selfstudy.tool.*
import java.util.*

class ArchiveFragment : Fragment() {
    private val viewModel: ArchiveViewModel by viewModels()
    private val binding : FragmentArchiveBinding by viewBinding()
    private val adapter = ArchiveAdapter(fragment = this)

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?):View{

        binding.recyclerView.let {
            it.adapter = adapter
            it.setHasFixedSize(true)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.bookListLiveData.observe(viewLifecycleOwner){
            binding.archiveNotFound.isVisible = it.isEmpty()
            adapter.submitList(it)
        }

        binding.toolbar.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }

        setDialogResultListener()
        setDatabaseListener()
        initSpeedDial()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        (activity as MainActivity).let {
            it.setSupportActionBar(binding.toolbar)

            NavigationUI.setupActionBarWithNavController(
                it, findNavController(), it.appBarConfiguration)
        }

    }
    private fun setDatabaseListener() {
        viewModel.databaseEvent.observe(viewLifecycleOwner){
            when(it?.first){
                "delete" -> "已刪除「${it.second?.getString("bookName")}」".showToast()
                "archive" -> "已取消封存「${it.second?.getString("bookName")}」".showToast()
                "update" -> "更新成功".showToast()
                "insertLocal" -> "已新增「${it.second?.getString("bookName")}」".showToast()
            }
        }
    }

    private fun setDialogResultListener() {
        setFragmentResultListener("DialogDeleteCommon") { _, _ ->
            viewModel.deleteBook()
        }
        setFragmentResultListener("EditBookFragment"){ _, bundle->
            val bookName = bundle.getString("bookName")!!
            val explanation = bundle.getString("explanation")!!
            viewModel.editBook(bookName, explanation)
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            if(binding.speedDialButton.mainButtonIsOpen){
                binding.speedDialButton.toggleChange(true)
                return@addCallback
            }

            findNavController().popBackStack()
        }
    }


    override fun onStart() {
        super.onStart()
        viewModel.calculateBookSize()
    }

    private fun initSpeedDial() {
        with(binding.speedDialButton){
            createChildButtonAndText(
                R.id.book_fragment_fab_add_word,
                R.drawable.ic_baseline_search_24,
                "新增單字"
            ){ button, textView ->
                findNavController().navigate(R.id.searchFragment)
            }
            createChildButtonAndText(
                R.id.book_fragment_fab_add_book,
                R.drawable.ic_baseline_bookmark_24,
                "新增題庫"
            ){ button, textView ->
                findNavController().navigate(R.id.addBookFragment)
            }
            mainButton.setOnLongClickListener {
                resources.getString(R.string.FAB_main).showToast()
                true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //自動連結與item id相同的fragment id
        return item.onNavDestinationSelected(findNavController()) ||
                super.onOptionsItemSelected(item)
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