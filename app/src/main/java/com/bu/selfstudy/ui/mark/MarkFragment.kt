package com.bu.selfstudy.ui.mark

import android.content.Context
import android.graphics.drawable.StateListDrawable
import androidx.appcompat.view.ActionMode
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.bu.selfstudy.ui.main.ActivityViewModel
import com.bu.selfstudy.ui.main.MainActivity
import com.bu.selfstudy.NavGraphDirections
import com.bu.selfstudy.R
import com.bu.selfstudy.databinding.FragmentMarkBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.tool.myselectiontracker.IdItemDetailsLookup
import com.bu.selfstudy.tool.myselectiontracker.IdItemKeyProvider
import com.bu.selfstudy.ui.book.ActionItemCreator


class MarkFragment : Fragment() {
    private val viewModel: MarkViewModel by viewModels()
    private val binding: FragmentMarkBinding by viewBinding()
    private val adapter = MarkAdapter(fragment = this)
    private val activityViewModel: ActivityViewModel by activityViewModels()

    private lateinit var tracker: SelectionTracker<Long>

    private var actionMode: ActionMode? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {

        binding.recyclerView.let {
            it.adapter = adapter
            it.setHasFixedSize(true)
        }
        return binding.root
    }


     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

         viewModel.wordListLiveData.observe(viewLifecycleOwner) {
             binding.markNotFound.root.isVisible = it.isEmpty()

             adapter.submitList(it)
             viewModel.refreshWordIdList(it)
         }

         viewModel.databaseEvent.observe(viewLifecycleOwner) {
             when (it?.first) {
                 "delete" -> return@observe
                 "mark" -> return@observe
                 "cancelMark" -> return@observe
                 "update" -> "更新已保存".showToast()
                 "copy"-> ("已複製 ${it.second?.get("count")} 個單字到 「${activityViewModel.getBookName(
                         it.second?.get("bookId") as Long)}」").showToast()
                 "move"-> ("已轉移 ${it.second?.get("count")} 個單字到 「${activityViewModel.getBookName(
                         it.second?.get("bookId") as Long)}」").showToast()
             }
         }


         binding.toolbar.setOnClickListener {
             findNavController().navigate(R.id.searchFragment)
         }


         setDialogResultListener()
         initSpeedDial()
         initFastScroll()
         initSelectionTracker()
     }


    private fun initFastScroll() {
        FastScroller(binding.recyclerView,
                requireContext().getDrawable(R.drawable.thumb_drawable) as StateListDrawable,
                requireContext().getDrawable(R.drawable.line_drawable),
                requireContext().getDrawable(R.drawable.thumb_drawable) as StateListDrawable,
                requireContext().getDrawable(R.drawable.line_drawable),
                resources.getDimensionPixelSize(androidx.recyclerview.R.dimen.fastscroll_default_thickness),
                resources.getDimensionPixelSize(androidx.recyclerview.R.dimen.fastscroll_minimum_range),
                resources.getDimensionPixelOffset(androidx.recyclerview.R.dimen.fastscroll_margin)
        )
    }

    private fun setDialogResultListener() {
        setFragmentResultListener("DialogDeleteCommon") { _, _ ->
            viewModel.longPressedWordIdList.let {
                viewModel.deleteWord(it)
            }
        }
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

        requireActivity().onBackPressedDispatcher.addCallback(this){
            if(binding.speedDialView.isOpen){
                binding.speedDialView.close()
                return@addCallback
            }

            findNavController().popBackStack()
        }
    }

    private fun initSelectionTracker() {
        if(::tracker.isInitialized)
            return

        tracker = SelectionTracker.Builder(
                "recycler-view-word-fragment",
                binding.recyclerView,
                IdItemKeyProvider(viewModel.wordIdList, true),
                IdItemDetailsLookup(binding.recyclerView, viewModel.wordIdList, true),
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything())
                .build().also {
                    adapter.tracker = it
                }

        val selectionObserver = object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                if (!tracker.hasSelection()) {
                    actionMode?.finish()
                } else {
                    viewModel.refreshLongPressedWord(tracker.selection.toList())
                    if (actionMode == null) {
                        actionMode = (activity as AppCompatActivity)
                                .startSupportActionMode(actionModeCallback)
                    }
                    actionMode?.title = "${tracker!!.selection.size()}/${viewModel.wordListLiveData.value?.size}"
                    actionMode?.invalidate()
                }
            }
            override fun onSelectionRestored() {
                super.onSelectionRestored()
                onSelectionChanged()
            }
        }

        tracker.addObserver(selectionObserver)
    }



    fun updateMarkWord(wordId:Long, isMark: Boolean){
        viewModel.updateMarkWord(wordId, isMark = isMark)
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


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        tracker.onRestoreInstanceState(savedInstanceState)
    }

    val actionModeCallback = object : ActionMode.Callback {
        /**對於搜尋結果進行操作, 會將ActionMode疊在SearchView上方, 此時鍵盤不會消失
        造成使用者能在看不到搜尋框時查詢, 因此ActionMode出現就必須關閉鍵盤
         */
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.word_action_mode, menu)
            closeKeyboard()
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean{
            menu.findItem(R.id.action_edit).isEnabled =
                    (viewModel.longPressedWordIdList.size == 1)

            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            actionModeMenuCallback(item.itemId)
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            tracker?.clearSelection()
            actionMode = null
        }
    }

    private fun actionModeMenuCallback(itemId: Int){
        when (itemId) {
            R.id.action_delete -> findNavController().navigate(
                    NavGraphDirections.actionGlobalDialogDeleteCommon(
                            "刪除單字",
                            "是否刪除 ${viewModel.longPressedWordIdList.size} 個單字?"
                    )
            )
            R.id.action_choose_all -> tracker!!.setItemsSelected(
                    viewModel.wordIdList,
                    viewModel.wordIdList.size != tracker!!.selection.size()
            )
            R.id.action_mark-> viewModel.updateMarkWord(
                    *viewModel.longPressedWordIdList.toLongArray(),
                    isMark = viewModel.longPressedWordIdList.any {
                        !viewModel.wordIdList.contains(it)
                    }
            )

            R.id.action_copy -> {
                viewModel.actionType = "copy"
                findNavController().navigate(
                        NavGraphDirections.actionGlobalDialogChooseBook(
                                title = "複製到題庫"
                        )
                )
            }
            R.id.action_move -> {
                viewModel.actionType = "move"
                findNavController().navigate(
                        NavGraphDirections.actionGlobalDialogChooseBook(
                                title = "轉移到題庫"
                        )
                )
            }
            R.id.action_edit->{
                viewModel.getWord(viewModel.longPressedWordIdList.first())?.let { word->
                    actionMode?.finish()
                    findNavController().navigate(
                            NavGraphDirections.actionGlobalEditWordFragment(word)
                    )
                }
            }
        }
    }
}

