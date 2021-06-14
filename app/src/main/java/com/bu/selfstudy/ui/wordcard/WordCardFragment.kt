package com.bu.selfstudy.ui.wordcard

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.FragmentWordCardBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.ui.book.BookFragmentDirections
import com.bu.selfstudy.ui.book.DialogEditBookArgs
import com.bu.selfstudy.ui.editword.EditWordViewModel
import com.bu.selfstudy.ui.wordlist.WordListViewModel
import com.leinardi.android.speeddial.SpeedDialActionItem
import kotlinx.coroutines.launch


class WordCardFragment : Fragment() {
    private val args: WordCardFragmentArgs by navArgs()
    private lateinit var viewModel: WordCardViewModel
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val binding: FragmentWordCardBinding by viewBinding()
    private val pagerAdapter = WordCardPagerAdapter()

    private var searchView: SearchView? = null
    private var viewPagerCallback: ViewPager2.OnPageChangeCallback? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        binding.viewPager.adapter = pagerAdapter
        lifecycle.addObserver(pagerAdapter)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }


     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         setHasOptionsMenu(true)

         lifecycleScope.launch {
             setDialogResultListener()
             initSpeedDial()
         }

         activityViewModel.currentOpenBookLiveData.value!!.let {
             viewModel = ViewModelProvider(this, WordCardViewModel.provideFactory(it))
                     .get(WordCardViewModel::class.java)
             (requireActivity() as AppCompatActivity).supportActionBar?.title = it.bookName
         }


         viewModel.wordListLiveData.observe(viewLifecycleOwner) {
             binding.explainView.visibility = if(it.isEmpty()) View.VISIBLE else View.INVISIBLE
             pagerAdapter.submitList(it)
             setPagerViewItem()
        }

         viewModel.isMarkLiveData.observe(viewLifecycleOwner){
             replaceMarkIcon(it)
         }


         viewPagerCallback = object: ViewPager2.OnPageChangeCallback(){
             override fun onPageSelected(position: Int) {
                 super.onPageSelected(position)
                 pagerAdapter.prepareMediaPlayer(position)
                 val realPosition = position % pagerAdapter.wordList.size
                 viewModel.updateCurrentPosition(realPosition)
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
        if(viewModel.firstLoad) {
            setPagerViewInitialItem()
            return
        }

        viewModel.currentPosition?.let {
            setPagerViewCurrentItem(it)
        }
    }

    private fun setPagerViewInitialItem() {
        if(viewModel.firstLoad) {
            viewModel.firstLoad = false
            val position = if (args.wordId != 0L)
                viewModel.getWordPosition(args.wordId)
            else
                viewModel.currentOpenBook.position

            setPagerViewCurrentItem(position)
        }
    }


    private fun setPagerViewCurrentItem(position: Int){
        binding.viewPager.post {
            binding.viewPager.setCurrentItem(
                pagerAdapter.mapToFakePosition(position), false
            )
        }
    }

    private fun setDialogResultListener() {
        setFragmentResultListener("ExitFromEditWordFragment") { _, _ ->
            "更新已保存".showToast()
        }
        setFragmentResultListener("delete") { _, _ ->
            viewModel.currentOpenWord?.let {
                viewModel.deleteWordToTrash(it.id)
            }
        }
    }


    private fun replaceMarkIcon(isMark: Boolean){

        val oldActionItem = if(isMark) ActionItemCreator.markItem else ActionItemCreator.cancelMarkItem
        val newActionItem = if(isMark) ActionItemCreator.cancelMarkItem else ActionItemCreator.markItem

        binding.speedDialView.actionItems.firstOrNull{
            it == oldActionItem
        }?.let {
            binding.speedDialView.replaceActionItem(oldActionItem, newActionItem)
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
                            val title = "刪除單字"
                            val message = "刪除此單字?"
                            val action = WordCardFragmentDirections.actionGlobalDialogDeleteCommon(title, message)
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
                this.close()
                return@setOnActionSelectedListener true // To keep the Speed Dial open
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_search -> {
            }
            R.id.action_edit_book -> {
                findNavController().navigate(R.id.wordListFragment)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.wordcard_toolbar, menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.viewPager.currentItem?.let {
            outState.putInt("position", it)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.getInt("position")?.let {
            binding.viewPager.post {
                binding.viewPager.setCurrentItem(it, false)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        var backPressedToExitOnce: Boolean = false

        requireActivity().onBackPressedDispatcher.addCallback(this){
            if(binding.speedDialView.isOpen){
                binding.speedDialView.close()
            }else{
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewPagerCallback?.let {
            binding.viewPager.unregisterOnPageChangeCallback(it)
        }
        viewPagerCallback = null
    }
}

/*



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



}*/

