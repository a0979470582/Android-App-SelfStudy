package com.bu.selfstudy.ui.trash

import android.graphics.drawable.StateListDrawable
import androidx.appcompat.view.ActionMode
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
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
import com.bu.selfstudy.data.model.DeleteRecord
import com.bu.selfstudy.databinding.FragmentTrashBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.tool.myselectiontracker.IdItemDetailsLookup
import com.bu.selfstudy.tool.myselectiontracker.IdItemKeyProvider
import com.bu.selfstudy.ui.wordlist.FastScroller
import kotlinx.coroutines.launch


class TrashFragment : Fragment() {
    private val viewModel: TrashViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val binding: FragmentTrashBinding by viewBinding()
    private val adapter = TrashAdapter(fragment = this)

    private lateinit var tracker: SelectionTracker<Long>

    private var searchView: SearchView? = null
    private var actionMode: ActionMode? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {

        binding.recyclerView.let {
            it.adapter = adapter
        }
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)


        viewModel.deleteRecordBook.observe(viewLifecycleOwner){
            viewModel.combineDeleteRecord()
        }
        viewModel.deleteRecordWord.observe(viewLifecycleOwner){
            viewModel.combineDeleteRecord()
        }

        viewModel.deleteRecord.observe(viewLifecycleOwner){
            adapter.submitList(it)
            refreshIdList(it)
            binding.notFoundImage.isVisible = it.isEmpty()
        }

        viewModel.databaseEvent.observe(viewLifecycleOwner){
            when(it?.first){
                "delete"-> return@observe
                "mark"->return@observe
                "cancelMark"->return@observe
            }
        }

        lifecycleScope.launch {
            initSelectionTracker()
            setDialogResultListener()
            initFastScroll()
        }


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
            /*viewModel.longPressedWordIdList.let {
                if(it.isNotEmpty())
                    viewModel.deleteWordToTrash(it)
            }*/
        }
    }

    private fun refreshIdList(recordList: List<DeleteRecord>){
        viewModel.refreshIdList(recordList)
    }

    private fun initSelectionTracker() {
        if(::tracker.isInitialized)
            return

        tracker = SelectionTracker.Builder(
                "recycler-view-word-fragment",
                binding.recyclerView,
                IdItemKeyProvider(viewModel.recordIdList),
                IdItemDetailsLookup(binding.recyclerView, viewModel.recordIdList),
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
                    viewModel.refreshLongPressedRecord(tracker.selection.toList())
                    if (actionMode == null) {
                        actionMode = (activity as AppCompatActivity)
                                .startSupportActionMode(actionModeCallback)
                    }
                    actionMode?.title = "${tracker.selection.size()}" +
                            "/" +
                            "${viewModel.deleteRecord.value?.size}"
                }
            }
        }

        tracker.addObserver(selectionObserver)
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


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        tracker.onRestoreInstanceState(savedInstanceState)
    }

    val actionModeCallback = object : androidx.appcompat.view.ActionMode.Callback {
        /**對於搜尋結果進行操作, 會將ActionMode疊在SearchView上方, 此時鍵盤不會消失
        造成使用者能在看不到搜尋框時查詢, 因此ActionMode出現就必須關閉鍵盤
         */
        override fun onCreateActionMode(mode: androidx.appcompat.view.ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.wordlist_action_mode, menu)
            closeKeyboard()
            return true
        }

        override fun onPrepareActionMode(mode: androidx.appcompat.view.ActionMode, menu: Menu) = false

        override fun onActionItemClicked(mode: androidx.appcompat.view.ActionMode, item: MenuItem): Boolean {
            actionModeMenuCallback(item.itemId)
            return true
        }

        override fun onDestroyActionMode(mode: androidx.appcompat.view.ActionMode) {
            tracker.clearSelection()
            actionMode = null
        }
    }

    private fun actionModeMenuCallback(itemId:Int){
        when (itemId) {
            R.id.action_delete -> {
                val title = "刪除"
                val message = "是否完全刪除 ${viewModel.longPressedRecordIdList.size} 個項目?"
                val action = TrashFragmentDirections.actionGlobalDialogDeleteCommon(title, message)
                findNavController().navigate(action)
            }
            R.id.action_choose_all -> {
                viewModel.recordIdList.let {
                    tracker.setItemsSelected(it, it.size != tracker.selection.size())
                }
            }
        }
    }

    fun refreshDeleteRecord(deleteRecord: DeleteRecord) {
        viewModel.refreshDeleteRecord(deleteRecord)
    }
}