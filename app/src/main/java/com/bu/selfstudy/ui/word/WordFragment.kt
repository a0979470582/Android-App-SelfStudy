package com.bu.selfstudy.ui.word

import android.content.Context
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.ui.main.ActivityViewModel
import com.bu.selfstudy.ui.main.MainActivity
import com.bu.selfstudy.NavGraphDirections
import com.bu.selfstudy.R
import com.bu.selfstudy.databinding.FragmentWordBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.tool.myselectiontracker.IdItemDetailsLookup
import com.bu.selfstudy.tool.myselectiontracker.IdItemKeyProvider
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


/**
 * 概念:
 * 初次載入: 卡片不易快速滑動, 需根據book-position或傳入wordId來定位,
 *         列表無須定位, 從起點開始即可
 *
 * 切換模式: 卡片切換至列表(下方按鈕), 要定位, 根據viewmodel內的紀錄即可
 *          列表切至卡片(下方按鈕), 無須定位, 但若是點擊列表單字來切換, 則需定位
 *
 * 回退: 進入順序card->list, 回退: list->card 同上, 無須定位
 *      進入順序card->list->card 回退 card->list->離開 須定位
 *      回退的定位方式與一般切換一樣即可
 *
 * 實作方式:
 * 初次載入, 如果是卡片模式, 等待wordList資料準備好再手動定位
 * 三種排版的切換, 都會根據viewModel.position?:0, 它只會被卡片模式的頁面變動影響
 *
 * 卡片模式的adapter重新與recyclerView連接, 都會先顯示第一項(0), 接著我們才能手動定位
 */
class WordFragment : Fragment() {
    private val args: WordFragmentArgs by navArgs()
    private val viewModel: WordViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val binding: FragmentWordBinding by viewBinding()

    private var drawerListener: DrawerLayout.DrawerListener? = null
    private val linearLayoutManager by lazy { LinearLayoutManagerWrapper(requireContext()) }

    //card
    private val cardAdapter by lazy { CardAdapter(fragment = this) }
    private val pagerSnapHelper by lazy { PagerSnapHelper() }
    private var onScrollListener: RecyclerView.OnScrollListener? = null

    //list
    private val listAdapter by lazy { ListAdapter(fragment = this) }
    private var actionMode: ActionMode? = null
    private var fastScroll: FastScroller? = null
    private var tracker: SelectionTracker<Long>? = null


    companion object{
        const val TYPE_CARD = 0
        const val TYPE_LIST = 1
        const val TYPE_SIMPLE = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.recyclerView.setHasFixedSize(true)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

         initRecyclerView()

         setSlideSheetListener()
         setDialogResultListener()

         viewModel.bookIdLiveData.value = args.bookId

         viewModel.bookLiveData.observe(viewLifecycleOwner){ book ->
             (requireActivity() as AppCompatActivity).supportActionBar?.title = book.bookName
         }

         //switchMap不會先發送第一次空值
         //觸發wordList變更有可能是傳入初始值, 修改, 刪除, 新增等情況,
         //假如刪除一列, 建議只notifyItem
         viewModel.wordListLiveData.observe(viewLifecycleOwner) {
             binding.wordNotFound.isVisible = it.isEmpty()

             if(viewModel.getLastBackStack() == TYPE_CARD){
                 cardAdapter.submitList(it)
                 setPagerViewItem()
             }else {
                 listAdapter.submitList(it)
             }

             viewModel.refreshWordIdList(it)
             viewModel.refreshMarkedWordIdList(it)
         }

         viewModel.markLiveData.observe(viewLifecycleOwner){
             replaceMarkIcon(it)
         }


        viewModel.databaseEvent.observe(viewLifecycleOwner) {
             when (it?.first) {
                 "delete" -> "您刪除了 ${it.second?.get("count")} 個單字".showToast()
                 "mark" -> resources.getString(R.string.toast_success_mark).showToast()
                 "cancelMark" -> resources.getString(R.string.toast_cancel_mark).showToast()
                 "update" -> "更新已保存".showToast()
                 "copy"-> ("已複製 ${it.second?.get("count")} 個單字到 「${activityViewModel.getBookName(
                         it.second?.get("bookId") as Long)}」").showToast()
                 "move"-> ("已轉移 ${it.second?.get("count")} 個單字到 「${activityViewModel.getBookName(
                         it.second?.get("bookId") as Long)}」").showToast()
             }
        }

        /**
         * 卡片的position主導其他顯示模式的position, 它會被記錄在Book,
         * 快速滑動時, 不會每一頁都被監聽到, 但不違背需求
         */
        onScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (viewModel.getLastBackStack() != TYPE_CARD)
                    return

                val position = linearLayoutManager.findFirstCompletelyVisibleItemPosition()

                if(position == -1)
                    return

                viewModel.updateCurrentPosition(cardAdapter.mapToRealPosition(position))
            }
        }
        binding.recyclerView.addOnScrollListener(onScrollListener!!)

        drawerListener = object: DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                if(viewModel.getLastBackStack() == TYPE_CARD)
                    setFabIsVisible(true)
            }

            //觸發順序: 拖動(1)->設定(2)->空閒(0), 設定的觸發非常快, 空閒的觸發很慢
            override fun onDrawerStateChanged(newState: Int) {
                if(
                    viewModel.getLastBackStack() == TYPE_CARD &&
                    newState == ViewDragHelper.STATE_SETTLING
                ) {
                    setFabIsVisible(false)
                }
            }
        }

        binding.slideSheet.addDrawerListener(drawerListener!!)
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

    override fun onDestroyView() {
        super.onDestroyView()
        setDrawerLock(false)
        binding.recyclerView.removeOnScrollListener(onScrollListener!!)
        onScrollListener = null
        binding.slideSheet.removeDrawerListener(drawerListener!!)
        drawerListener = null
    }


    //1. 初始載入 2. 配置改變, 退回此頁, 單字列表長度有變更(刪除或新增)
    private fun setPagerViewItem() {
        if(viewModel.getLastBackStack() != TYPE_CARD)
            return

        val position = if(viewModel.firstLoad)
            getInitialPosition()
        else
            viewModel.currentPosition?: 0


        setPagerViewCurrentItem(position)
    }

    fun setPagerViewCurrentItem(position: Int, smooth: Boolean = false) {
        binding.recyclerView.scrollToPosition(
            cardAdapter.mapToFakePosition(position)
        )
    }

    fun downloadAudio(wordId: Long) {
        viewModel.downloadAudio(wordId)
    }

    fun replaceMarkIcon(isMark: Boolean){
        binding.speedDialButton.changeIconAndText(
            R.id.fab_mark_word,
            if(isMark) R.drawable.ic_baseline_star_24 else R.drawable.ic_round_star_border_24,
            if(isMark) "取消標記" else "標記"
        )
    }

    fun updateMarkWord(wordId: Long, isMark: Boolean){
        viewModel.updateMarkWord(wordId, isMark = isMark)
    }


    private fun initRecyclerView(){
        binding.recyclerView.layoutManager = linearLayoutManager

        if(viewModel.getLastBackStack() == null)
            switchRecyclerView(args.type)
        else
            switchRecyclerView(
                type = viewModel.getLastBackStack()!!,
                position = viewModel.currentPosition ?: -1,
            )
    }

    fun switchRecyclerView(type: Int, position:Int = -1) {
        viewModel.addBackStack(type)

        with(binding.recyclerView) {
            when (type) {
                TYPE_CARD -> {
                    fastScroll?.destroyCallbacks()
                    pagerSnapHelper.attachToRecyclerView(binding.recyclerView)
                    linearLayoutManager.orientation = RecyclerView.HORIZONTAL
                    adapter = cardAdapter
                    viewModel.wordListLiveData.value?.let {
                        cardAdapter.submitList(it)
                    }
                    setChipState(R.id.chipCard, true)
                    initSpeedDial()
                }
                TYPE_LIST, TYPE_SIMPLE  -> {
                    fastScroll?.attachToRecyclerView(binding.recyclerView)
                    pagerSnapHelper.attachToRecyclerView(null)
                    linearLayoutManager.orientation = RecyclerView.VERTICAL
                    adapter = listAdapter
                    listAdapter.setTranslationIsVisible(type == TYPE_LIST)
                    viewModel.wordListLiveData.value?.let {
                        listAdapter.submitList(it)
                    }
                    setChipState(if(type== TYPE_LIST) R.id.chipList else R.id.chipSimple, true)
                }
            }
        }

        if(position != -1){
            if(type == TYPE_CARD)
                setPagerViewCurrentItem(position)
            else {
                binding.recyclerView.scrollToPosition(if(position==0) 0 else position+1)
            }
        }

        runRecyclerViewAnimation()


        if(type!= TYPE_CARD){
            if(tracker == null)
                initTracker()

            if(fastScroll == null )
                initFastScroll()
        }

        setFabIsVisible(
            type == TYPE_CARD && !binding.slideSheet.isDrawerOpen(GravityCompat.END)
        )

        setDrawerLock(type == TYPE_CARD)
    }

    private fun setDrawerLock(isLock: Boolean){
        if(isLock){
            binding.slideSheet.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            activity?.findViewById<DrawerLayout>(R.id.drawerLayout)?.setDrawerLockMode(
                    DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }else{
            binding.slideSheet.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            activity?.findViewById<DrawerLayout>(R.id.drawerLayout)?.setDrawerLockMode(
                    DrawerLayout.LOCK_MODE_UNLOCKED)
        }
    }

    fun setFabIsVisible(isVisible: Boolean){
        binding.speedDialButton.isVisible = isVisible
    }

    fun setChipState(chipId: Int, isChecked: Boolean){
        binding.navView.getHeaderView(0)?.run{
            findViewById<Chip>(chipId)?.isChecked = isChecked
        }
    }

    private fun runRecyclerViewAnimation() = with(binding.recyclerView){
            layoutAnimation = AnimationUtils.loadLayoutAnimation(
                requireContext(), R.anim.layout_animation)
            adapter!!.notifyDataSetChanged()
            scheduleLayoutAnimation()
        }


    private fun getInitialPosition():Int {
        if(viewModel.firstLoad) {
            viewModel.firstLoad = false

            return if (args.wordId != 0L)
                viewModel.getWordPosition(args.wordId)
            else
                viewModel.bookLiveData.value?.position?:0
            //若無Book存在, 其實也不會觸發WordList及此方法
        }
        throw Exception(" firstLoad isn't true. ")
    }

    private fun setDialogResultListener() {
        setFragmentResultListener("EditWordFragment") { _, _ ->
            "更新已保存".showToast()
        }
        setFragmentResultListener("DialogDeleteCommon") { _, _ ->
            if(viewModel.getLastBackStack() == TYPE_CARD)
                viewModel.currentOpenWord?.let {
                    viewModel.deleteWord(it.id)
                }
            else
                viewModel.longPressedWordIdList.let {
                    if(it.isNotEmpty()) {
                        actionMode?.finish()
                        viewModel.deleteWord(*it.toLongArray())
                    }
                }
        }
        setFragmentResultListener("DialogChooseBook"){ string, bundle->
            actionMode?.finish()
            when(viewModel.actionType){
                "copy"->viewModel.copyWord(bundle.getLong("bookId"))
                "move"->viewModel.moveWord(bundle.getLong("bookId"))
            }
        }
    }

    private fun setSlideSheetListener() {
        binding.navView.getHeaderView(0)?.run {
            findViewById<Button>(R.id.closeIcon).setOnClickListener {
                binding.slideSheet.closeDrawer(GravityCompat.END)
            }
            findViewById<Button>(R.id.resetIcon).setOnClickListener {
                findViewById<Chip>(R.id.chipCard).isChecked = true
                findViewById<RadioButton>(R.id.buttonAll).isChecked = true
                findViewById<RadioButton>(R.id.buttonOldest).isChecked = true
            }

            findViewById<ChipGroup>(R.id.radioGroupMode).setOnCheckedChangeListener { _, checkedId ->
                val type = when (checkedId) {
                    R.id.chipCard -> TYPE_CARD
                    R.id.chipList -> TYPE_LIST
                    R.id.chipSimple -> TYPE_SIMPLE
                    else -> TYPE_CARD
                }
                if(viewModel.getLastBackStack() != type)
                    switchRecyclerView(type, viewModel.currentPosition ?: 0)
            }
            findViewById<RadioGroup>(R.id.radioGroupLabel).setOnCheckedChangeListener { _, checkedId ->
                actionMode?.finish()
                viewModel.onlyMarkLiveData.value = when(checkedId){
                    R.id.buttonAll -> false
                    R.id.buttonMark -> true
                    else -> false
                }
            }
            findViewById<RadioGroup>(R.id.radioGroupOrder).setOnCheckedChangeListener { _, checkedId ->
                with(viewModel.SortStateEnum){
                    viewModel.sortStateLiveData.value = when(checkedId){
                        R.id.buttonOldest -> OLDEST
                        R.id.buttonNewest -> NEWEST
                        R.id.buttonAZ -> AZ
                        R.id.buttonZA -> ZA
                        else -> OLDEST
                    }
                }
            }
        }
    }


    private fun initFastScroll() {
        if(fastScroll != null)
            return

            fastScroll = FastScroller(
                binding.recyclerView,
                requireContext().getDrawable(R.drawable.thumb_drawable) as StateListDrawable,
                requireContext().getDrawable(R.drawable.line_drawable),
                requireContext().getDrawable(R.drawable.thumb_drawable) as StateListDrawable,
                requireContext().getDrawable(R.drawable.line_drawable),
                resources.getDimensionPixelSize(androidx.recyclerview.R.dimen.fastscroll_default_thickness),
                resources.getDimensionPixelSize(androidx.recyclerview.R.dimen.fastscroll_minimum_range),
                resources.getDimensionPixelOffset(androidx.recyclerview.R.dimen.fastscroll_margin)
            )
    }

    /**
     * 1. SelectionTracker內部使用EventBridge類來連接RecyclerView, 它無法動態更改Adapter,
     * 因此在使用正確的Adapter時再初始化SelectionTracker
     *
     * 2. 批量移除與tracker連接的資料時要先將其關閉, 它會浪費資源走訪被移除的資料列, 及有機率拋出異常
     */
    private fun initTracker() {
        if(tracker != null)
            return

        tracker = SelectionTracker.Builder(
            "recycler-view-word-fragment",
            binding.recyclerView,
            IdItemKeyProvider(viewModel.wordIdList, true),
            IdItemDetailsLookup(binding.recyclerView, viewModel.wordIdList, true),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything())
                .build().also {
                    listAdapter.tracker = it
                }

        val selectionObserver = object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()

                if(viewModel.getLastBackStack() == TYPE_CARD)
                    return

                if (!tracker!!.hasSelection()) {
                    actionMode?.finish()
                } else {
                    viewModel.refreshLongPressedWord(tracker!!.selection.toList())//ArrayList
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

        tracker!!.addObserver(selectionObserver)

    }

    private fun initSpeedDial() {
        with(binding.speedDialButton){
            mainButton.setOnLongClickListener {
                resources.getString(R.string.FAB_main).showToast()
                true
            }

            createChildButtonAndText(
                R.id.fab_add_word,
                R.drawable.ic_baseline_search_24,
                "新增"
            ){ button, textView ->
                findNavController().navigate(
                    NavGraphDirections.actionGlobalSearchFragment(
                        bookId = viewModel.bookIdLiveData.value?:0
                    )
                )
            }

            createChildButtonAndText(
                R.id.fab_edit_word,
                R.drawable.ic_baseline_edit_24,
                "編輯"
            ){ button, textView ->
                viewModel.currentOpenWord?.let {
                    findNavController().navigate(
                        NavGraphDirections.actionGlobalEditWordFragment(it)
                    )
                }
            }

            createChildButtonAndText(
                R.id.fab_delete_word,
                R.drawable.ic_round_delete_24,
                "刪除"
            ){ button, textView ->
                viewModel.currentOpenWord?.let {
                    findNavController().navigate(
                        NavGraphDirections.actionGlobalDialogDeleteCommon(
                            "刪除單字",
                            "刪除此單字?"
                        )
                    )
                }
            }

            createChildButtonAndText(
                R.id.fab_mark_word,
                R.drawable.ic_round_star_border_24,
                "標記"
            ){ button, textView ->
                viewModel.currentOpenWord?.let {
                    viewModel.updateMarkWord(it.id, isMark = !viewModel.markLiveData.value!!)
                }
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            when {
                binding.speedDialButton.mainButtonIsOpen ->
                    binding.speedDialButton.toggleChange(true)

                binding.slideSheet.isDrawerOpen(GravityCompat.END) ->
                    binding.slideSheet.closeDrawer(GravityCompat.END)

                else -> popBackStack()
            }
        }
    }

    private fun popBackStack(){
        if(viewModel.wordListLiveData.value.isNullOrEmpty()) {
            findNavController().popBackStack(R.id.bookFragment, false)
            return
        }

        val previousType = viewModel.popBackStack()
        if(previousType == null)
            findNavController().popBackStack(R.id.bookFragment, false)
        else
            switchRecyclerView(previousType, viewModel.currentPosition?:-1)
    }

    /**
     * wordcard和wordlist都採用singleTask
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_search -> findNavController().navigate(
                NavGraphDirections.actionGlobalSearchFragment(
                    bookId = viewModel.bookIdLiveData.value?:0
                )
            )
            R.id.action_filter -> with(binding.slideSheet) {
                if (isDrawerOpen(GravityCompat.END))
                    closeDrawer(GravityCompat.END)
                else
                    openDrawer(GravityCompat.END)
            }
            android.R.id.home->{
                findNavController().popBackStack(R.id.bookFragment, false)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.word_toolbar, menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker?.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        tracker?.onRestoreInstanceState(savedInstanceState)

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
                        !viewModel.markedWordIdList.contains(it)
                    }
                )

            R.id.action_copy -> {
                viewModel.actionType = "copy"
                findNavController().navigate(
                        NavGraphDirections.actionGlobalDialogChooseBook(
                                title = "複製到題庫",
                                bookId = viewModel.bookIdLiveData.value!!
                        )
                )
            }
            R.id.action_move -> {
                viewModel.actionType = "move"
                findNavController().navigate(
                        NavGraphDirections.actionGlobalDialogChooseBook(
                                title = "轉移到題庫",
                                bookId = viewModel.bookIdLiveData.value!!
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

    fun getCurrentBookId() = viewModel.bookIdLiveData.value

}