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

    private lateinit var tracker: SelectionTracker<Long>

    private var actionMode: ActionMode? = null


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
            findNavController().navigate(R.id.recentWordFragment)
        }

        lifecycleScope.launch {
            initSpeedDial()
            initSelectionTracker()
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

            this.setOnChangeListener(object: SpeedDialView.OnChangeListener {
                override fun onMainActionSelected() = false

                override fun onToggleChanged(isOpen: Boolean) {
                    if(isOpen) actionMode?.finish()//快速播號開啟時關閉actionMode
                }
            })
        }
    }

    private fun initSelectionTracker() {
        if(::tracker.isInitialized)
            return

        tracker = SelectionTracker.Builder(
                "recycler-view-book-fragment",
                binding.recyclerView,
                IdItemKeyProvider(activityViewModel.bookIdList),
                IdItemDetailsLookup(binding.recyclerView, activityViewModel.bookIdList),
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectSingleAnything())
         .build().also {
             adapter.tracker = it
         }

        val selectionObserver = object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                if (!tracker.hasSelection()) {
                    actionMode?.finish()
                } else {
                    activityViewModel.bookListLiveData.value?.let {
                        viewModel.refreshLongPressedBook(it, tracker.selection.first())
                    }
                    if (actionMode == null) {
                        actionMode = (activity as AppCompatActivity)
                                .startSupportActionMode(actionModeCallback)
                    }
                }
            }
        }

        tracker.addObserver(selectionObserver)
    }

    //set actionMode, for multiple selection
    private val actionModeCallback = object : ActionMode.Callback {
        /**對於搜尋結果進行操作, 會將ActionMode疊在SearchView上方, 此時鍵盤不會消失
        造成使用者能在看不到搜尋框時查詢, 因此ActionMode出現就必須關閉鍵盤
         */
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.book_action_mode, menu)
            closeKeyboard()
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            actionModeMenuCallback(item?.itemId)
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            tracker.clearSelection()
            actionMode = null
        }
    }

    private fun actionModeMenuCallback(itemId: Int){
        when (itemId) {
            R.id.action_delete -> {
                viewModel.longPressedBook?.let {
                    val action = BookFragmentDirections.actionGlobalDialogDeleteCommon(
                            "刪除題庫", "刪除「${it.bookName}」?"
                    )
                    findNavController().navigate(action)
                }
            }
            R.id.action_edit -> {
                viewModel.longPressedBook?.let {
                    val action = BookFragmentDirections.actionBookFragmentToEditBookDialog(
                            it.bookName
                    )
                    findNavController().navigate(action)
                }
            }
            R.id.action_archive -> {
                viewModel.longPressedBook?.let {
                }
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(::tracker.isInitialized)
            tracker.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if(::tracker.isInitialized)
            tracker.onRestoreInstanceState(savedInstanceState)
    }
}