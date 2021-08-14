package com.bu.selfstudy.ui.search

import android.app.SearchManager
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.MainActivity
import com.bu.selfstudy.NavGraphDirections
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.SearchHistory
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.FragmentSearchBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.tool.dropdowntextview.DropdownTextView
import com.bu.selfstudy.tool.dropdowntextview.SelectionTextCallback
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * 焦點在搜尋框時: 秀出建議列表
 * 發送搜尋時: 秀出進度條
 * 搜尋完畢時: 秀出單字卡或找不到之訊息
 * 重新聚焦在搜尋框時: 秀出建議列表
 * 退出建議列表時: 回到上一頁, 若已進行搜尋則回到該頁
 */
class SearchFragment: Fragment()  {

    private val binding : FragmentSearchBinding by viewBinding()
    private val args: SearchFragmentArgs by navArgs()
    private val adapter = SuggestionListAdapter(this)
    private val viewModel: SearchViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by activityViewModels()

    private lateinit var searchView: SearchView

    private var mediaPlayer: MediaPlayer? = null
    private var scrollFlags: Int? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?):View{

        binding.recyclerView.let {
            it.adapter = this.adapter
            it.setHasFixedSize(true)
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        viewModel.searchQuery.value = ""//let it get data

        this@SearchFragment.refreshClipboardText()

        viewModel.suggestionList.observe(viewLifecycleOwner){
            lifecycleScope.launch { adapter.submitList(it) }
        }

        viewModel.wordLiveData.observe(viewLifecycleOwner){
            showWordCard(it)
        }

        binding.wordCardItem.soundButton.setOnClickListener {
            viewModel.wordLiveData.value?.getOrNull()?.let {
                if(it.audioFilePath.isBlank())
                    return@setOnClickListener

                mediaPlayer?.apply {
                    reset()
                    setDataSource(it.audioFilePath)
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                    setOnPreparedListener{
                        it.start()
                    }
                    prepareAsync()
                }
            }
        }

        binding.extendedFab.setOnClickListener {
            findNavController().navigate(
                NavGraphDirections.actionGlobalDialogChooseBook(
                    "加入至題庫",
                    bookId = args.bookId
                )
            )
        }

        //監聽從DialogChooseBook返回的bookId
        setFragmentResultListener("DialogChooseBook"){_, bundle ->
            viewModel.wordLiveData.value?.getOrNull()?.let {
                it.bookId = bundle.getLong("bookId")
                activityViewModel.insertWord(it)
            }
        }

        setSelectionTextCallback(object: SelectionTextCallback {
            override fun onSelectionTextChanged(text: String?) {
                if(text.isNullOrBlank())
                    return

                startSearch(query = text)
            }
        })

    }

    fun setSelectionTextCallback(callback: SelectionTextCallback){
        with(binding.wordCardItem){
            translationTextView.setSelectionTextCallback(callback)
            variationTextView.setSelectionTextCallback(callback)
            exampleTextView.setSelectionTextCallback(callback)
            synonymsTextView.setSelectionTextCallback(callback)
            noteTextView.setSelectionTextCallback(callback)
        }
    }

    override fun onStart() {
        super.onStart()
        mediaPlayer = MediaPlayer()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun refreshClipboardText() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.refreshClipboardText(requireActivity().getClipboardText())
        }
    }

    /**
     *     根據搜尋結果新增單字後, 因檢視而進入單字卡頁, 會觸發view的重建
     *     因此要回復SearchView的狀態
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.only_search_toolbar, menu)

        searchView = menu.findItem(R.id.action_search).actionView as SearchView

        with(searchView){

            setPadding(-44, 0, 0, 0)//修改hint的位置

            //移除搜尋框內的search_icon
            findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
                    ?.setImageDrawable(null)



            setIconifiedByDefault(false)//展開SearchView
            requestFocus()//加入光標
            openKeyboard()//開鍵盤

            //加入麥克風功能
            setSearchableInfo((requireActivity()
                    .getSystemService(Context.SEARCH_SERVICE) as SearchManager)
                    .getSearchableInfo(requireActivity().componentName))

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String) = false

                override fun onQueryTextChange(query: String): Boolean {

                    if (query.isNullOrBlank())
                        this@SearchFragment.refreshClipboardText()

                    query.trim().let {
                        viewModel.searchQuery.value = it
                        adapter.refreshSearchQuery(it)//set span
                    }

                    return false
                }
            })

            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if(hasFocus){
                    showSearchSuggestion()
                }
            }
        }

        //從別的fragment回彈後要回復狀態
        if(binding.wordCardItem.root.isVisible)
            searchView.setQuery(viewModel.lastSearchQuery, false)


        if(args.query.isNotEmpty()) {
            startSearch(args.query)
            closeKeyboard()
        }

    }

    //使用者可能關閉鍵盤而不是回上一頁
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                closeKeyboard()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    fun removeSearchHistory(searchHistory: SearchHistory) {
        viewModel.removeSearchHistory(searchHistory)
    }


    //此方法會在使用語音查詢時從MainActivity調用
    fun startSearch(query: String){
        binding.wordCardItem.scrollView.scrollTo(0, 0)
        binding.progressBar.visibility = View.VISIBLE
        searchView.setQuery(query, false)
        searchView.clearFocus()//顯示結果時關閉鍵盤
        viewModel.addOneSearchHistory(query.trim())//紀錄搜尋結果
        viewModel.getWordPage(query)
        if(!hasNetwork())
            "目前沒有網路連接".showToast()
    }

    //FAB會隨word-card一起出現, 且在建議列表出現時消失
    private fun showWordCard(result: Result<Word>) {
        val word = result.getOrNull()
        binding.progressBar.isVisible = false
        binding.recyclerView.isVisible = false
        if(word != null){
            binding.wordCardItem.let {
                it.root.visibility = View.VISIBLE
                it.word = word
                it.wordInfo.text = "Dr.eye 譯典通"
            }
            binding.extendedFab.isVisible = true
        }else{
            binding.wordCardItem.root.isVisible = false
            binding.searchNotFound.root.isVisible = true
        }
    }
    private fun showSearchSuggestion(){
        binding.recyclerView.visibility = View.VISIBLE
        binding.wordCardItem.root.visibility = View.INVISIBLE
        binding.extendedFab.isVisible = false
        binding.searchNotFound.root.visibility = View.INVISIBLE
    }



    /**
     * 結果頁(包含搜尋不到而顯示的背景圖)的返回就是上一頁(題庫頁或其他)
     * 如果目前顯示搜尋建議列表, 且使用者第一次進入SearchFragment, 就同結果頁的行為,
     * 如果使用者不是第一次進入, 則應顯示上一次搜尋結果
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            if(binding.searchNotFound.root.isVisible ||
               binding.wordCardItem.root.isVisible
            ){
                findNavController().popBackStack()
            }

            if(viewModel.lastSearchQuery == "")
                findNavController().popBackStack()
            else{
                searchView.setQuery(viewModel.lastSearchQuery, false)
                if(viewModel.wordLiveData.value == null)
                    binding.searchNotFound.root.visibility = View.VISIBLE
                else
                    showWordCard(viewModel.wordLiveData.value!!)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity as MainActivity).let {
            it.setSupportActionBar(binding.toolbar)

            NavigationUI.setupActionBarWithNavController(
                it, findNavController(), it.appBarConfiguration)
        }

    }

    override fun onDetach() {
        super.onDetach()
        if (scrollFlags == null)
            return

        val layout = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        val params = layout.layoutParams as AppBarLayout.LayoutParams

        params.scrollFlags = scrollFlags!!

        scrollFlags = null
    }

}