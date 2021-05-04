package com.bu.selfstudy.ui.word


import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionTracker
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.FragmentWordBinding
import com.bu.selfstudy.tool.*
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.coroutines.launch


class WordFragment : Fragment() {
    private val viewModel: WordViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by activityViewModels()

    private val binding: FragmentWordBinding by viewBinding()

    private val adapter = WordAdapter()
    private val slideAdapter = WordSlideAdapter()
    private lateinit var tracker: SelectionTracker<Long>
    private var searchView: SearchView? = null

    private lateinit var viewPagerCallback: ViewPager2.OnPageChangeCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.slideAdapter = slideAdapter
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        getNavigationResultLiveData<Boolean>("isDelete")?.observe(viewLifecycleOwner){
            "getNavigationResultLiveData".log()
            if(it) activityViewModel.deleteWordToTrash(viewModel.currentWord!!.id)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            initSpeedDial(savedInstanceState == null)
        }


        /**
         * ViewPager要特別處理三種情況
         * 1. 第一次開啟此頁面
         * 2. 配置變更
         * 3. 題庫切換
         */
        activityViewModel.bookLiveData.observe(viewLifecycleOwner) { newBook ->
            "newBook:${newBook}".log()
            if(viewModel.currentBook == null){
                viewModel.bookIdLiveData.value = newBook.id
            }

            //切換題庫的情況
            viewModel.currentBook?.let {
                if(it.id != newBook.id){
                    viewModel.updateInitialWordId()//保存舊題庫
                    viewModel.isInitialPage = true
                    viewModel.bookIdLiveData.value = newBook.id
                }
            }
            viewModel.currentBook = newBook
        }

        viewModel.wordListLiveData.observe(viewLifecycleOwner) {
            slideAdapter.setWordList(it)
            "size:${it.size}".log()
            if (viewModel.isInitialPage) {
                viewModel.isInitialPage = false
                val fakePosition: Int = it.size*100+viewModel.getInitialPosition()
                binding.viewPager.post {
                    fakePosition.log()
                    binding.viewPager.setCurrentItem(fakePosition, false)
                }
            }
        }


        viewPagerCallback = object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(binding.progressIndicator.visibility != View.GONE)
                    binding.progressIndicator.visibility = View.GONE
                val realPosition = position%slideAdapter.wordList.size
                viewModel.currentPosition = realPosition
                viewModel.currentWord = viewModel.wordListLiveData.value!![realPosition]
            }
        }
        binding.viewPager.registerOnPageChangeCallback(viewPagerCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPager.unregisterOnPageChangeCallback(viewPagerCallback)
    }


    private fun initSpeedDial(addActionItems: Boolean) {
        if (addActionItems) {
            val doMark = SpeedDialActionItem.Builder(
                R.id.fab_mark_word, R.drawable
                    .ic_round_star_border_24
            )
                    .setLabel("標記")
                    .create()

            val disMark = SpeedDialActionItem.Builder(
                R.id.fab_dis_mark_word, R.drawable
                    .ic_baseline_star_24
            )
                    .setLabel("取消標記")
                    .create()

            binding.speedDialView
                    .addActionItem(
                        SpeedDialActionItem.Builder(
                            R.id.fab_add_word, R.drawable
                                .ic_baseline_add_24
                        )
                            .setLabel("新增")
                            .create()
                    )

            binding.speedDialView
                    .addActionItem(
                        SpeedDialActionItem.Builder(
                            R.id.fab_edit_word, R.drawable
                                .ic_outline_text_snippet_24
                        )
                            .setLabel("編輯")
                            .create()
                    )

            binding.speedDialView
                    .addActionItem(
                        SpeedDialActionItem.Builder(
                            R.id.fab_delete_word, R.drawable
                                .ic_round_delete_24
                        )
                            .setLabel("刪除")
                            .create()
                    )

            binding.speedDialView.addActionItem(doMark)
        }

        // Set option fabs click listeners.
        binding.speedDialView.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->

            val doMark = SpeedDialActionItem.Builder(
                R.id.fab_mark_word, R.drawable
                    .ic_round_star_border_24
            )
                .setLabel("標記")
                .setLabelClickable(false)
                .create()

            val disMark = SpeedDialActionItem.Builder(
                R.id.fab_dis_mark_word, R.drawable
                    .ic_baseline_star_24
            )
                .setLabel("取消標記")
                .setLabelClickable(false)
                .create()
            when (actionItem.id) {
                R.id.fab_add_word -> {
                    findNavController().navigate(R.id.addWordFragment)
                    return@OnActionSelectedListener false // false will close it without animation
                }
                R.id.fab_edit_word -> {
                    "編輯".showToast()
                    return@OnActionSelectedListener false // false will close it without animation
                }
                R.id.fab_delete_word -> {
                    val action = WordFragmentDirections.actionGlobalToDeleteDialog("刪除這 1 個單字?")
                    findNavController().navigate(action)
                    return@OnActionSelectedListener false // false will close it without animation
                }
                R.id.fab_mark_word -> {
                    binding.speedDialView.replaceActionItem(actionItem, disMark)
                    "標記成功".showToast()
                    return@OnActionSelectedListener true // false will close it without animation
                }
                R.id.fab_dis_mark_word -> {
                    binding.speedDialView.replaceActionItem(actionItem, doMark)
                    "取消標記".showToast()
                    return@OnActionSelectedListener true
                }
            }
            true // To keep the Speed Dial open
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_search -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.word_toolbar, menu)

    }

    override fun onStop() {
        super.onStop()
        viewModel.updateInitialWordId()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //tracker?.onSaveInstanceState(outState)
        binding.viewPager.currentItem?.let {
            outState.putInt("position", it)
        }

    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        //tracker?.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.getInt("position")?.let {
            binding.viewPager.post {
                binding.viewPager.setCurrentItem(it, false)
            }
        }
    }
}

        /*
        binding.recyclerView.let {
            it.adapter = this.adapter
            it.setHasFixedSize(true)
        }

        tracker = buildSelectionTracker(
                binding.recyclerView,
                viewModel.idList,
                SelectionPredicates.createSelectAnything(),
                R.menu.word_action_mode,
                ::onActionItemClicked
        ).also { adapter.tracker = it }




        /**db event*/
        getDatabaseResult(viewModel.databaseEventLD){_, message, _ ->
            message.showToast()
        }

        /**dialog event*/
        getDialogResult()
    }
*/
/*
    private fun onActionItemClicked(itemId:Int){
        when (itemId) {
            R.id.action_delete -> {
                val action = WordFragmentDirections.actionGlobalToDeleteDialog(
                        "是否刪除 ${tracker.selection.size()} 個單字 ?")
                findNavController().navigate(action)
            }
            R.id.action_choose_all -> {
                viewModel.idList.let {
                    tracker.setItemsSelected(it,it.size != tracker.selection.size()
                    )
                }
            }

            R.id.action_copy -> {
                navigateToChooseBookDialog("copy")
            }
            R.id.action_move -> {
                navigateToChooseBookDialog("move")
            }
        }
    }


    /**遺珠之憾:1. 空query時旋轉螢幕無法保留介面*/
    /**在這裡產生searchView對象*/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.word_toolbar, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val searchManager = requireActivity().getSystemService(SEARCH_SERVICE) as SearchManager
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

        searchView?.apply{
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                /**避免按下送出後, 鍵盤消失, 但輸入框的光標仍在閃爍*/
                override fun onQueryTextSubmit(query: String): Boolean {
                    clearFocus()
                    return false
                }

                /**每一次搜尋文字改變, 就觸發資料庫刷新*/
                override fun onQueryTextChange(query: String): Boolean {
                    viewModel.searchQueryLD.value = query
                    return false
                }
            })

            /**如果頁面重建, ViewModel保有查詢字串, 表示先前頁面銷毀時, 使用者正在使用查詢
            我們知道頁面銷毀會使SearchView也銷毀, 但SearchView在第一次開啟或最後銷毀時, 都會
            觸發onQueryTextChange且query是空值, 注意expandActionView就會觸發此情形*/
            val pendingQuery = viewModel.searchQueryLD.value
            if(pendingQuery!=null && pendingQuery.isNotBlank()){
                searchItem.expandActionView()
                setQuery(pendingQuery, false)
                clearFocus()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add->{
                navigateToAddWordFragment()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToChooseBookDialog(action:String){
        viewModel.bookListLD.value?.let {
            val bookList = it.filter { it.id != viewModel.bookId }
            val action = WordFragmentDirections.actionGlobalChooseBookDialog(
                    bookList.map { it.bookName }.toTypedArray(),
                    bookList.map { it.id }.toLongArray(),
                    "選擇一個題庫",
                    action
            )
            findNavController().navigate(action)
        }
    }

    private fun navigateToAddWordFragment(){
        val action = WordFragmentDirections.actionGlobalAddWordFragment(
                args.bookId,
                viewModel.bookListLD.value!!.map { it.bookName }.toTypedArray(),
                viewModel.bookListLD.value!!.map { it.id }.toLongArray()
        )
        findNavController().navigate(action)
    }

    private fun getDialogResult(){
        getDialogResult {dialogName, bundle ->
            when(dialogName){
                "ToDeleteDialog"->{
                    if(bundle.getBoolean("isDelete"))
                        viewModel.deleteWordsToTrash(tracker.selection.toList())
                }
                "ChooseBookDialog"->{
                    bundle.getLong("bookId").let {bookId->
                        when(bundle.getString("action")){
                            "copy"->viewModel.copyWordsTo(tracker.selection.toList(), bookId)
                            "move"->viewModel.moveWordsTo(tracker.selection.toList(), bookId)
                        }
                    }
                }
                "AddWordFragment"->{
                    bundle.getParcelable<Word>("word").let { word->
                        viewModel.insertWords(listOfNotNull(word))
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        tracker.onRestoreInstanceState(savedInstanceState)
    }
}*/


/*viewModel.positionLiveData.observe(viewLifecycleOwner){
    "VWPosition:$it--currentItem:${binding.viewPager.currentItem}".log()
    if (binding.viewPager.currentItem != it)
        binding.viewPager.setCurrentItem(it, false)
}*/