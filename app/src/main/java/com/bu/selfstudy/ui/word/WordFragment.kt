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
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.bu.selfstudy.NavGraphDirections
import com.bu.selfstudy.R
import com.bu.selfstudy.databinding.FragmentWordBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.tool.myselectiontracker.IdItemDetailsLookup
import com.bu.selfstudy.tool.myselectiontracker.IdItemKeyProvider
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.coroutines.launch


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
    private val binding: FragmentWordBinding by viewBinding()

    private val cardAdapter = CardAdapter(fragment = this)
    private val listAdapter = ListAdapter(fragment = this)
    private val listAdapter2 = ListAdapter2(fragment = this)


    private val pagerSnapHelper = PagerSnapHelper()


    private var actionMode: ActionMode? = null
    private var h_LayoutManager: LinearLayoutManager? = null
    private var v_LayoutManager: LinearLayoutManager? = null

    private var onScrollListener: RecyclerView.OnScrollListener? = null
    private var drawerListener: DrawerLayout.DrawerListener? = null
    private var fastScroll: FastScroller? = null

    private var tracker: SelectionTracker<Long>? = null


    private lateinit var speedDialView: SpeedDialView

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

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         setHasOptionsMenu(true)


         lifecycleScope.launch {
             initLayoutManager()
             initRecyclerView(savedInstanceState)
             setSlideSheetListener()
             setDialogResultListener()
         }

         viewModel.bookIdLiveData.value = args.bookId

         viewModel.bookLiveData.observe(viewLifecycleOwner){ book ->
             (requireActivity() as AppCompatActivity).supportActionBar?.title = book.bookName
         }

         //switchMap不會先發送第一次空值
         //觸發wordList變更有可能是傳入初始值, 修改, 刪除, 新增等情況,
         //假如刪除一列, 建議只notifyItem
         viewModel.wordListLiveData.observe(viewLifecycleOwner) {
             binding.explainImage.visibility = if(it.isEmpty()) View.VISIBLE else View.INVISIBLE
             binding.explainText.visibility = if(it.isEmpty()) View.VISIBLE else View.INVISIBLE

             cardAdapter.submitList(it)
             setPagerViewItem()

             listAdapter.submitList(it)
             viewModel.refreshWordIdList(it)

             listAdapter2.submitList(it)

         }

         viewModel.markLiveData.observe(viewLifecycleOwner){
             replaceMarkIcon(it)
         }


        viewModel.databaseEvent.observe(viewLifecycleOwner) {
             when (it?.first) {
                 "delete" -> "您刪除了 ${it.second?.get("count")} 個單字".showToast()
                 "mark" -> return@observe
                 "cancelMark" -> return@observe
                 "update" -> "更新已保存".showToast()
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

                val position = h_LayoutManager!!.findFirstCompletelyVisibleItemPosition()

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
        initSpeedDial()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.removeOnScrollListener(onScrollListener!!)
        onScrollListener = null
        binding.slideSheet.removeDrawerListener(drawerListener!!)
        drawerListener = null
    }

    private fun initLayoutManager() {
        if(h_LayoutManager == null)
            h_LayoutManager = LinearLayoutManagerWrapper(requireContext(), HORIZONTAL, false)
        if(v_LayoutManager == null)
            v_LayoutManager = LinearLayoutManagerWrapper(requireContext(), VERTICAL, false)

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

        val oldActionItem = if(isMark) ActionItemCreator.markItem else ActionItemCreator.cancelMarkItem
        val newActionItem = if(isMark) ActionItemCreator.cancelMarkItem else ActionItemCreator.markItem

        speedDialView.actionItems.firstOrNull{
            it == oldActionItem
        }?.let {
            speedDialView.replaceActionItem(oldActionItem, newActionItem)
        }
    }

    fun updateMarkWord(wordId: Long, isMark: Boolean){
        viewModel.updateMarkWord(wordId, isMark)
    }


    private fun initRecyclerView(savedInstanceState: Bundle?){
        if(savedInstanceState == null)
            switchRecyclerView(args.type)
        else
            switchRecyclerView(
                savedInstanceState["type"] as Int,
                viewModel.currentPosition ?: -1,
            )
    }

    fun switchRecyclerView(type: Int, position:Int = -1) {

        if(type != viewModel.getLastBackStack())
            actionMode?.finish()

        viewModel.addBackStack(type)

        with(binding.recyclerView) {
            when (type) {
                TYPE_CARD -> {
                    layoutManager = h_LayoutManager
                    adapter = cardAdapter
                    pagerSnapHelper.attachToRecyclerView(binding.recyclerView)
                    setChipState(R.id.chipCard, true)
                    fastScroll?.destroyCallbacks()
                }
                TYPE_LIST -> {
                    layoutManager = v_LayoutManager
                    adapter = listAdapter2
                    pagerSnapHelper.attachToRecyclerView(null)
                    setChipState(R.id.chipList, true)
                    fastScroll?.attachToRecyclerView(binding.recyclerView)
                }
                TYPE_SIMPLE -> {
                    layoutManager = v_LayoutManager
                    adapter = listAdapter
                    pagerSnapHelper.attachToRecyclerView(null)
                    setChipState(R.id.chipSimple, true)
                    fastScroll?.attachToRecyclerView(binding.recyclerView)
                }
            }

            runRecyclerViewAnimation()

            if(position != -1){
                if(type == TYPE_CARD)
                    setPagerViewCurrentItem(position)
                else {
                    scrollToPosition(position + 1)
                }
            }

            setFabIsVisible(
                    type == TYPE_CARD &&
                    !binding.slideSheet.isDrawerOpen(GravityCompat.END)
            )

            if(type!= TYPE_CARD){
                if(tracker == null)
                    initTracker()

                if(fastScroll == null )
                    initFastScroll()

            }

            if(type == TYPE_CARD){
                activity?.findViewById<DrawerLayout>(R.id.drawerLayout)?.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                binding.slideSheet.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }else{
                activity?.findViewById<DrawerLayout>(R.id.drawerLayout)?.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_UNLOCKED)
                binding.slideSheet.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

            }
        }
    }

    fun setFabIsVisible(isVisible: Boolean){
        requireActivity().findViewById<SpeedDialView>(
                R.id.speedDialView)?.isVisible = isVisible
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
                    if(it.isNotEmpty())
                        viewModel.deleteWord(*it.toLongArray())
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
     * SelectionTracker內部使用EventBridge類來連接RecyclerView, 它無法動態更改Adapter,
     * 因此在使用正確的Adapter時再初始化SelectionTracker
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
                    listAdapter2.tracker = it
                }

        val selectionObserver = object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()

                if(viewModel.getLastBackStack() == TYPE_CARD)
                    return

                if (!tracker!!.hasSelection()) {
                    actionMode?.finish()
                } else {
                    viewModel.refreshLongPressedWord(tracker!!.selection.toList())
                    if (actionMode == null) {
                        actionMode = (activity as AppCompatActivity)
                                .startSupportActionMode(actionModeCallback)
                    }
                    actionMode?.title = "${tracker!!.selection.size()}/${viewModel.wordListLiveData.value?.size}"
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
        speedDialView = requireActivity().findViewById(R.id.speedDialView)

        with(speedDialView){
            setMainFabClosedDrawable(resources.getDrawable(R.drawable.ic_baseline_edit_24))

            mainFab.setOnLongClickListener {
                resources.getString(R.string.FAB_main).showToast()
                return@setOnLongClickListener true
            }

            clearActionItems()
            addAllActionItems(
                listOf(
                    ActionItemCreator.addItem,
                    ActionItemCreator.editItem,
                    ActionItemCreator.deleteItem,
                    ActionItemCreator.markItem
                )
            )

            // Set option fabs click listeners.
            this.setOnActionSelectedListener { actionItem ->
                if(actionItem.id == R.id.fab_add_word)
                    findNavController().navigate(R.id.searchFragment)

                if(viewModel.wordListLiveData.value.isNullOrEmpty())
                    return@setOnActionSelectedListener false

                when (actionItem.id) {
                    R.id.fab_edit_word -> {
                        viewModel.currentOpenWord?.let {
                            findNavController().navigate(
                                NavGraphDirections.actionGlobalEditWordFragment(it)
                            )
                        }
                    }
                    R.id.fab_delete_word -> {
                        viewModel.currentOpenWord?.let {
                            findNavController().navigate(
                                NavGraphDirections.actionGlobalDialogDeleteCommon(
                                    "刪除單字",
                                    "刪除此單字?"
                                )
                            )
                        }
                    }
                    R.id.fab_mark_word -> {
                        viewModel.currentOpenWord?.let {
                            replaceActionItem(actionItem, ActionItemCreator.cancelMarkItem)
                            viewModel.updateMarkWord(it.id, true)
                            resources.getString(R.string.toast_success_mark).showToast()
                        }
                    }
                    R.id.fab_cancel_mark_word -> {
                        viewModel.currentOpenWord?.let {
                            replaceActionItem(actionItem, ActionItemCreator.markItem)
                            viewModel.updateMarkWord(it.id, false)
                            resources.getString(R.string.toast_cancel_mark).showToast()
                        }
                    }
                }
                return@setOnActionSelectedListener false //關閉小按鈕
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            when {
                speedDialView.isOpen -> speedDialView.close()

                binding.slideSheet.isDrawerOpen(GravityCompat.END) ->
                    binding.slideSheet.closeDrawer(GravityCompat.END)

                else -> popBackStack()
            }
        }
    }

    private fun popBackStack(){
        if(viewModel.wordListLiveData.value.isNullOrEmpty()) {
            findNavController().popBackStack()
            return
        }

        val previousType = viewModel.popBackStack()
        if(previousType == null)
            findNavController().popBackStack()
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
        outState.putInt("type", viewModel.getLastBackStack()!!)
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

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

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
            R.id.action_delete -> {
                findNavController().navigate(
                    NavGraphDirections.actionGlobalDialogDeleteCommon(
                        "刪除單字",
                        "是否刪除 ${viewModel.longPressedWordIdList.size} 個單字?"
                    )
                )
            }
            R.id.action_choose_all -> {
                tracker!!.setItemsSelected(
                    viewModel.wordIdList,
                    viewModel.wordIdList.size != tracker!!.selection.size()
                )
            }

            R.id.action_copy -> {
                //navigateToChooseBookDialog("copy")
            }
            R.id.action_move -> {
                //navigateToChooseBookDialog("move")
                /*viewModel.ChoosedBook?.let {
                    val action = BookFragmentDirections.actionBookFragmentToEditBookDialog(it.bookName)
                    findNavController().navigate(action)
                }*/
            }
        }
    }

    fun getCurrentBookId() = viewModel.bookIdLiveData.value

}