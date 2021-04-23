package com.bu.selfstudy.ui.word


import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.doOnAttach
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bu.selfstudy.MainActivity
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.FragmentWordBinding
import com.bu.selfstudy.tools.showToast
import com.bu.selfstudy.tools.*
import com.bu.selfstudy.ui.ActivityViewModel
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView

class WordFragment : Fragment() {
    private val viewModel: WordViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by activityViewModels()

    private val binding: FragmentWordBinding by viewBinding()

    private val adapter = WordAdapter()
    private val slideAdapter = WordSlideAdapter()
    private lateinit var tracker: SelectionTracker<Long>
    private var searchView: SearchView? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.slideAdapter = slideAdapter
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        initSpeedDial(savedInstanceState == null)
        /**
         activityViewModel的生命週期為整個APP, 它儲存在導覽抽屜顯示的會員資料,
         取出單字所需的題庫列表, 他們佔用內存不多
         */
        activityViewModel.bookLiveData.observe(viewLifecycleOwner){
                viewModel.bookLiveData.value = it
        }

        viewModel.wordListLiveData.observe(viewLifecycleOwner){
            slideAdapter.setWordList(it)
        }


        /**
            這裡流程為
            positionLiveData(0)
            initialPositionLiveData(4)
            positionLiveData(4)
            onPageSelected(4)
            positionLiveData(4)

            1. 一旦開始observe, 系統底層使用異步讀取SQLite, 而只有positionLiveData
                保存初始值為0, 因此他最先被觀察到, 他等於viewpager的預設item也就是0,
                因此不執行setCurrentItem
            2. 接著initialPositionLiveData從SQLite返回某值(例如4), 將isInitial關閉,
                接著設置positionLiveData並使它被觀察到, 這時positionLiveData就不是0而是4,
                它與viewPager目前的item(0)不一樣, 因此執行setCurrentItem
            3. viewPager被設定Page(也就是執行setCurrentItem), 會同步到positionLiveData,
                因此又觸發一次positionLiveData的觀察, 但這時它等於viewPager目前的item, 不會重複
                執行setCurrentItem
         */
        viewModel.initialPositionLiveData.observe(viewLifecycleOwner){
            if(viewModel.isInitial){
                viewModel.isInitial = false
                viewModel.positionLiveData.value = it
            }
        }

        viewModel.positionLiveData.observe(viewLifecycleOwner){
            if (binding.viewPager.currentItem != it)
                binding.viewPager.setCurrentItem(it, false)
        }


        binding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.positionLiveData.value = position
            }
        })
    }


    private fun initSpeedDial(addActionItems: Boolean) {
        if (addActionItems) {
            val doMark = SpeedDialActionItem.Builder(R.id.fab_mark_word, R.drawable
                    .ic_round_star_border_24)
                    .setLabel("標記")
                    .setLabelClickable(false)
                    .create()

            val disMark = SpeedDialActionItem.Builder(R.id.fab_dis_mark_word, R.drawable
                    .ic_baseline_star_24)
                    .setLabel("取消標記")
                    .setLabelClickable(false)
                    .create()



            binding.speedDial
                    .addActionItem(SpeedDialActionItem.Builder(R.id.fab_add_word, R.drawable
                            .ic_baseline_add_24)
                    .setLabel("新增")
                    .setLabelClickable(false)
                    .create())

            binding.speedDial
                    .addActionItem(SpeedDialActionItem.Builder(R.id.fab_edit_word, R.drawable
                            .ic_outline_text_snippet_24)
                            .setLabel("編輯")
                            .setLabelClickable(false)
                            .create())

            binding.speedDial
                    .addActionItem(SpeedDialActionItem.Builder(R.id.fab_delete_word, R.drawable
                            .ic_round_delete_24)
                            .setLabel("刪除")
                            .setLabelClickable(false)
                            .create())

            binding.speedDial.addActionItem(doMark)
        }

        // Set option fabs clicklisteners.
        binding.speedDial.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->

            val doMark = SpeedDialActionItem.Builder(R.id.fab_mark_word, R.drawable
                    .ic_round_star_border_24)
                    .setLabel("標記")
                    .setLabelClickable(false)
                    .create()

            val disMark = SpeedDialActionItem.Builder(R.id.fab_dis_mark_word, R.drawable
                    .ic_baseline_star_24)
                    .setLabel("取消標記")
                    .setLabelClickable(false)
                    .create()
            when (actionItem.id) {
                R.id.fab_add_word -> {
                    "新增被點了".showToast()
                    return@OnActionSelectedListener false // false will close it without animation
                }
                R.id.fab_edit_word -> {
                    "編輯".showToast()
                    return@OnActionSelectedListener false // false will close it without animation
                }
                R.id.fab_delete_word -> {
                    "刪除".showToast()
                    return@OnActionSelectedListener false // false will close it without animation
                }
                R.id.fab_mark_word -> {
                    binding.speedDial.replaceActionItem(actionItem, disMark)
                    "標記成功".showToast()
                    return@OnActionSelectedListener true // false will close it without animation
                }
                R.id.fab_dis_mark_word->{
                    binding.speedDial.replaceActionItem(actionItem, doMark)
                    "取消標記".showToast()
                    return@OnActionSelectedListener true
                }
            }
            true // To keep the Speed Dial open
        })
    }

    override fun onStop() {
        super.onStop()
        if(!viewModel.isInitial)
            viewModel.updateInitialWordId()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_search->{
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.word_toolbar, menu)

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


        viewModel.wordListLD.observe(viewLifecycleOwner){ words ->
            adapter.setWordList(words)
            viewModel.setIdList(words)
            viewModel.isLoadingLD.value = false
        }
        viewModel.bookListLD.observe(viewLifecycleOwner){}

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