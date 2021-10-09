package com.bu.selfstudy.ui.book


import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.ui.main.MainActivity
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.databinding.FragmentBookBinding
import com.bu.selfstudy.tool.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/**
 * RecyclerView Item更新動畫重疊的原因:
 * 1. 從後一頁面返回此頁時，若adapter還保有ViewHolder暫存，由於所有View重設值，子項的高度會先歸零再放大為內容高度
 * 2. Item重設造成的，可以使用payloads對子項進行局部更新，也避免在判斷物件相等時花費太多時間
 */
class BookFragment : Fragment() {

    private val viewModel: BookViewModel by viewModels()
    private val binding : FragmentBookBinding by viewBinding()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?):View{

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val adapter = BookAdapter(fragment = this)

        lifecycleScope.launchWhenStarted{
            delay(1000)
            //continue...
        }

        binding.recyclerView.let {
            it.adapter = adapter
            it.setHasFixedSize(true)
        }

        viewModel.bookListLiveData.observe(viewLifecycleOwner){
            binding.bookNotFound.isVisible = it.isEmpty()
            adapter.submitList(it)
        }

        binding.toolbar.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }

        viewModel.databaseEvent.observe(viewLifecycleOwner){
            when(it?.first){
                "delete" -> "已刪除「${it.second?.getString("bookName")}」".showToast()
                "archive" -> "已封存「${it.second?.getString("bookName")}」".showToast()
                "update" -> "更新成功".showToast()
                "insertLocal" -> "已新增「${it.second?.getString("bookName")}」".showToast()
            }
        }

        setDialogResultListener()
        initSpeedDial()
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setNewToolbar(binding.toolbar)
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


    override fun onAttach(context: Context) {
        super.onAttach(context)
        var backPressedToExitOnce: Boolean = false


        requireActivity().onBackPressedDispatcher.addCallback(this){
            if(binding.speedDialButton.mainButtonIsOpen){
                binding.speedDialButton.toggleChange(true)
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