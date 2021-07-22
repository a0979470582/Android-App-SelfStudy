package com.bu.selfstudy.ui.wordcard

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.R
import com.bu.selfstudy.databinding.FragmentWordCardBinding
import com.bu.selfstudy.tool.*
import kotlinx.coroutines.launch
import java.lang.Exception


class WordCardFragment : Fragment() {
    private val args: WordCardFragmentArgs by navArgs()
    private val viewModel: WordCardViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val binding: FragmentWordCardBinding by viewBinding()
    private val pagerAdapter = WordCardPagerAdapter(fragment = this)

    private var viewPagerCallback: ViewPager2.OnPageChangeCallback? = null
    private var firstLoadViewPager = true

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        binding.viewPager.adapter = pagerAdapter
        binding.lifecycleOwner = viewLifecycleOwner

        lifecycle.addObserver(pagerAdapter)//for create media in adapter

        return binding.root
    }


     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         setHasOptionsMenu(true)

         lifecycleScope.launch {
             setDialogResultListener()
             initSpeedDial()
         }

         activityViewModel.getBookWithId(args.bookId)?.let { book ->
             viewModel.bookLiveData.value = book
             (requireActivity() as AppCompatActivity)
                     .supportActionBar?.title = book.bookName
         }

         //switchMap不會先發送第一次空值
         //觸發wordList變更有可能是傳入初始值, 修改, 刪除, 新增等情況,
         //假如刪除一列, 建議只notifyItem
         viewModel.wordListLiveData.observe(viewLifecycleOwner) {
             binding.explainImage.visibility = if(it.isEmpty()) View.VISIBLE else View.INVISIBLE
             binding.explainText.visibility = if(it.isEmpty()) View.VISIBLE else View.INVISIBLE
             pagerAdapter.submitList(it)
             setPagerViewItem()
        }

         viewModel.markLiveData.observe(viewLifecycleOwner){
             replaceMarkIcon(it)
         }

         /**
          * 修正頁數ViewPager的時機:
          * 初始化頁面時, 退回到此頁時(ViewModel存在), 配置變更(同退回到此頁時),
          * 刪除單字或新增單字造成List長度改變(待解決)
          */
         viewPagerCallback = object: ViewPager2.OnPageChangeCallback(){
             override fun onPageSelected(position: Int) {
                 super.onPageSelected(position)
                 if(!firstLoadViewPager){
                     viewModel.updateCurrentPosition(
                             pagerAdapter.mapToRealPosition(position))
                 }
                 firstLoadViewPager = false
             }
         }

         binding.viewPager.registerOnPageChangeCallback(viewPagerCallback!!)

         viewModel.databaseEvent.observe(viewLifecycleOwner){
             when(it?.first){
                 "delete"-> "您刪除了一個單字".showToast()
                 "mark"->return@observe
                 "cancelMark"->return@observe
                 "update"->"更新已保存".showToast()
             }
         }
    }

    private fun setPagerViewItem() {
        if(viewModel.firstLoad){//初始載入
            setPagerViewCurrentItem(getInitialPosition())
        }else{//配置改變, 退回此頁, 單字列表長度有變更(刪除或新增)
            setPagerViewCurrentItem(viewModel.currentPosition?: 0)
        }
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


    private fun setPagerViewCurrentItem(position: Int, smooth: Boolean=false){
        binding.viewPager.post {
            binding.viewPager.setCurrentItem(
                    pagerAdapter.mapToFakePosition(position), smooth
            )
        }
    }

    private fun setDialogResultListener() {
        setFragmentResultListener("EditWordFragment") { _, _ ->
            "更新已保存".showToast()
        }
        setFragmentResultListener("DialogDeleteCommon") { _, _ ->
            viewModel.currentOpenWord?.let {
                //pagerAdapter.notifyRemoveOneWord(viewModel.currentPosition!!)
                viewModel.deleteWordToTrash(it.id)
            }
        }
    }


    fun replaceMarkIcon(isMark: Boolean){

        val oldActionItem = if(isMark) ActionItemCreator.markItem else ActionItemCreator.cancelMarkItem
        val newActionItem = if(isMark) ActionItemCreator.cancelMarkItem else ActionItemCreator.markItem

        binding.speedDial.actionItems.firstOrNull{
            it == oldActionItem
        }?.let {
            binding.speedDial.replaceActionItem(oldActionItem, newActionItem)
        }
    }

    private fun initSpeedDial() {
        if(binding.speedDial.actionItems.isNotEmpty())
            return

        with(binding.speedDial){
            this.mainFab.setOnLongClickListener {
                resources.getString(R.string.FAB_main).showToast()
                return@setOnLongClickListener true
            }

            this.addAllActionItems(listOf(
                    ActionItemCreator.addItem,
                    ActionItemCreator.editItem,
                    ActionItemCreator.deleteItem,
                    ActionItemCreator.markItem)
            )

            // Set option fabs click listeners.
            this.setOnActionSelectedListener { actionItem ->
                when (actionItem.id) {
                    R.id.fab_add_word -> {
                        findNavController().navigate(R.id.addWordFragment)
                    }
                    R.id.fab_edit_word -> {
                        viewModel.currentOpenWord?.let {
                            val action = WordCardFragmentDirections.actionGlobalEditWordFragment(it)
                            findNavController().navigate(action)
                        }
                    }
                    R.id.fab_delete_word -> {
                        viewModel.currentOpenWord?.let {
                            val action = WordCardFragmentDirections.actionGlobalDialogDeleteCommon(
                                    "刪除單字", "刪除此單字?"
                            )
                            findNavController().navigate(action)
                        }
                    }
                    R.id.fab_mark_word -> {
                        viewModel.currentOpenWord?.let {
                            this.replaceActionItem(actionItem, ActionItemCreator.cancelMarkItem)
                            resources.getString(R.string.toast_success_mark).showToast()
                            viewModel.updateMarkWord(it.id, true)
                        }
                        return@setOnActionSelectedListener true
                    }
                    R.id.fab_cancel_mark_word -> {
                        viewModel.currentOpenWord?.let {
                            this.replaceActionItem(actionItem, ActionItemCreator.markItem)
                            resources.getString(R.string.toast_cancel_mark).showToast()
                            viewModel.updateMarkWord(it.id, false)
                        }
                        return@setOnActionSelectedListener true
                    }
                }
                return@setOnActionSelectedListener false //關閉小按鈕
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            if(binding.speedDial.isOpen){
                binding.speedDial.close()
            }else{
                findNavController().popBackStack()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_search -> {
                findNavController().navigate(R.id.searchFragment)
            }
            R.id.action_edit_book -> {
                viewModel.bookLiveData.value?.let {
                    findNavController().navigate(WordCardFragmentDirections
                            .actionWordCardFragmentToWordListFragment(bookId = it.id))
                }

            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.wordcard_toolbar, menu)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewPagerCallback?.let {
            binding.viewPager.unregisterOnPageChangeCallback(it)
        }
        viewPagerCallback = null
    }

    fun downloadAudio(wordId: Long) {
        viewModel.downloadAudio(wordId)
    }

}