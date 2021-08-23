package com.bu.selfstudy.ui.book


import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.bu.selfstudy.MainActivity
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.databinding.FragmentBookBinding
import com.bu.selfstudy.tool.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class BookFragment : Fragment() {

    private val viewModel: BookViewModel by viewModels()
    private val binding : FragmentBookBinding by viewBinding()
    private val adapter = BookAdapter(fragment = this)

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
            binding.bookNotFound.root.isVisible = it.isEmpty()
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
                "archive" -> "已封存「${it.second?.getString("bookName")}」".showToast()
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
        setFragmentResultListener("DialogChooseLocalBook"){_, bundle->
            "下載題庫中...".showToast()
            val bookName = bundle.getString("bookName")!!
            val explanation = bundle.getString("explanation")!!
            val storageRef = FirebaseStorage
                    .getInstance()
                    .reference
                    .child("local_book/${bookName}.json")


            val file = File.createTempFile("test", "json")

            storageRef.getFile(file).addOnSuccessListener {
                "加入題庫中...".showToast()
                viewModel.insertLocalBook(bookName, explanation, file)
            }.addOnFailureListener {
                if(!hasNetwork())
                    "開啟網路來下載更多內容".showToast()
                else
                    "下載未完成".showToast()
            }


        }
    }

    private fun initSpeedDial() {
        if(binding.speedDialView.actionItems.isNotEmpty())
            return

        with(binding.speedDialView){

            mainFab.setOnLongClickListener {
                resources.getString(R.string.FAB_main).showToast()
                return@setOnLongClickListener true
            }

            addActionItem(ActionItemCreator.addWordItem)
            addActionItem(ActionItemCreator.addBookItem)

            this.setOnActionSelectedListener { actionItem ->
                when (actionItem.id) {
                    R.id.book_fragment_fab_add_word -> {
                        findNavController().navigate(R.id.searchFragment)
                    }
                    R.id.book_fragment_fab_add_book -> {
                        findNavController().navigate(R.id.addBookFragment)
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


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.book_toolbar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //自動連結與item id相同的fragment id
        return item.onNavDestinationSelected(findNavController()) ||
                super.onOptionsItemSelected(item)
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