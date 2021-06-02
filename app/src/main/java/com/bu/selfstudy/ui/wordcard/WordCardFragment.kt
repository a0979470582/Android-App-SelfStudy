package com.bu.selfstudy.ui.wordcard

import androidx.appcompat.view.ActionMode
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.viewpager2.widget.ViewPager2
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.FragmentWordBinding
import com.bu.selfstudy.databinding.FragmentWordCardBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.tool.myselectiontracker.IdItemDetailsLookup
import com.bu.selfstudy.tool.myselectiontracker.IdItemKeyProvider
import com.bu.selfstudy.ui.word.WordListAdapter
import com.leinardi.android.speeddial.SpeedDialActionItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class WordCardFragment : Fragment() {
    private val wordCardViewModel: WordCardViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val binding: FragmentWordCardBinding by viewBinding()
    private val pagerAdapter = WordCardPagerAdapter()
    private val args: WordCardFragmentArgs by navArgs()

    private var searchView: SearchView? = null
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var viewPagerCallback: ViewPager2.OnPageChangeCallback

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        binding.viewPager.adapter = pagerAdapter
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }


     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         setHasOptionsMenu(true)


         lifecycleScope.launch {
             initSpeedDial(savedInstanceState == null)
         }

        activityViewModel.currentOpenBookLiveData.observe(viewLifecycleOwner) {
            wordCardViewModel.currentOpenBookLiveData.value = it
        }

        wordCardViewModel.wordListLiveData.observe(viewLifecycleOwner) {
            System.currentTimeMillis().log()

            pagerAdapter.submitList(it)
            setPagerViewCurrentItem(wordCardViewModel.currentPosition?: args.position)
        }


        viewPagerCallback = object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val realPosition = position%pagerAdapter.wordList.size
                wordCardViewModel.currentPosition = realPosition
                wordCardViewModel.currentOpenWord = pagerAdapter.wordList[realPosition]
                wordCardViewModel.updateInitialWordId()
            }
        }
        binding.viewPager.registerOnPageChangeCallback(viewPagerCallback)

         wordCardViewModel.updateEvent.observe(this){
             "已儲存成功".showToast()
         }

         wordCardViewModel.markEvent.observe(this){
             when(it){
                 "mark" -> resources.getString(R.string.toast_success_mark).showToast()
                 "cancel_mark" -> resources.getString(R.string.toast_cancel_mark).showToast()
             }
         }

         wordCardViewModel.insertEvent.observe(this){
             binding.root.showSnackbar("已新增了${it!!.size}個單字", "檢視"){
                 "正在檢視中...".showToast()
             }
         }

         wordCardViewModel.deleteEvent.observe(this){
             "已移除${it}個單字".showToast()
         }
         wordCardViewModel.deleteToTrashEvent.observe(this){
             binding.root.showSnackbar("已將${it}個單字移至回收桶", "回復"){
                 "正在回復中...".showToast()
             }
         }
    }

    private fun showExplainView() {
        if(binding.explainView.isVisible){
            return
        }
        binding.explainView.visibility = View.VISIBLE
    }
    private fun hideExplainView() {
        if(!binding.explainView.isVisible){
            return
        }
        binding.explainView.visibility = View.GONE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        var backPressedToExitOnce: Boolean = false

        requireActivity().onBackPressedDispatcher.addCallback(this){
            if(binding.speedDialView.isOpen){
                binding.speedDialView.close()
                return@addCallback
            }
        }
    }


    private fun setPagerViewCurrentItem(position: Int){
        wordCardViewModel.wordListLiveData.value?.let{
            if (it.size >= 2) {
                val fakePosition: Int = it.size * 100 + position
                binding.viewPager.post {
                    binding.viewPager.setCurrentItem(fakePosition, false)
                }
            }
        }
    }

    private fun initSpeedDial(addActionItems: Boolean) {
        val speedDialView = binding.speedDialView

        speedDialView.mainFab.setOnLongClickListener {
            resources.getString(R.string.FAB_main).showToast()
            true
        }


        val doMark = SpeedDialActionItem.Builder(
                R.id.fab_mark_word, R.drawable
                .ic_round_star_border_24
        ).setLabel(resources.getString(R.string.FAB_mark)).create()

        val disMark = SpeedDialActionItem.Builder(
                R.id.fab_cancel_mark_word, R.drawable
                .ic_baseline_star_24
        ).setLabel(resources.getString(R.string.FAB_cancel_mark)).create()

        if (addActionItems) {
            speedDialView.addActionItem(
                    SpeedDialActionItem.Builder(
                            R.id.fab_add_word, R.drawable
                            .ic_baseline_add_24
                    ).setLabel(resources.getString(R.string.FAB_add)).create()
            )

            speedDialView.addActionItem(
                    SpeedDialActionItem.Builder(
                            R.id.fab_edit_word, R.drawable
                            .ic_outline_text_snippet_24
                    ).setLabel(resources.getString(R.string.FAB_edit)).create()
            )

            speedDialView.addActionItem(
                    SpeedDialActionItem.Builder(
                            R.id.fab_delete_word, R.drawable
                            .ic_round_delete_24
                    ).setLabel(resources.getString(R.string.FAB_delete)).create()
            )

            speedDialView.addActionItem(doMark)
        }

        // Set option fabs click listeners.
        speedDialView.setOnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.fab_add_word -> {
                    findNavController().navigate(R.id.addWordFragment)
                }
                R.id.fab_edit_word -> {
                    wordCardViewModel.currentOpenWord?.let {
                        val action = WordCardFragmentDirections.actionGlobalEditWordFragment(it.id)
                        findNavController().navigate(action)
                    }

                }
                R.id.fab_delete_word -> {
                    wordCardViewModel.currentOpenWord?.let {
                        val action = WordCardFragmentDirections.actionGlobalToDeleteDialog("刪除這 1 個單字?", wordCardViewModel.currentOpenWord!!.id)
                        findNavController().navigate(action)
                    }
                }
                R.id.fab_mark_word -> {
                    wordCardViewModel.currentOpenWord?.let {
                        markWord(it)
                        speedDialView.replaceActionItem(actionItem, disMark)
                        resources.getString(R.string.toast_success_mark).showToast()
                    }
                }
                R.id.fab_cancel_mark_word -> {
                    wordCardViewModel.currentOpenWord?.let {
                        cancelMarkWord(it)
                        speedDialView.replaceActionItem(actionItem, doMark)
                        resources.getString(R.string.toast_cancel_mark).showToast()
                    }
                }
            }
            speedDialView.close()
            true // To keep the Speed Dial open
        }
    }

    fun markWord(word:Word){
        val word = word.copy()
        word.isMark = true
        wordCardViewModel.markWord(word)
    }
    fun cancelMarkWord(word:Word){
        val word = word.copy()
        word.isMark = false
        wordCardViewModel.markWord(word)
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
        inflater.inflate(R.menu.word_card_toolbar, menu)
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

    override fun onStart() {
        super.onStart()
        if(mediaPlayer == null){
            mediaPlayer = MediaPlayer()
        }
        pagerAdapter.mediaPlayer = mediaPlayer
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null
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

