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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.bu.selfstudy.ui.main.ActivityViewModel
import com.bu.selfstudy.ui.main.MainActivity
import com.bu.selfstudy.NavGraphDirections
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.SearchHistory
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.FragmentSearchBinding
import com.bu.selfstudy.tool.*
import com.bu.selfstudy.tool.dropdowntextview.SelectionTextCallback
import com.google.android.material.snackbar.Snackbar
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
    private val adapter = SuggestionListAdapter(fragment = this)
    private val viewModel: SearchViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by activityViewModels()

    private lateinit var searchView: SearchView

    private var mediaPlayer: MediaPlayer? = null

    private var showingSnackbar: Snackbar? = null

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

        //注意viewModel是懶加載, 第一次調用在不同線程會錯誤
        refreshClipboardText()

        viewModel.searchQuery.value = ""

        viewModel.suggestionList.observe(viewLifecycleOwner){
             adapter.submitList(it)
        }

        viewModel.wordLiveData.observe(viewLifecycleOwner){
            showResult(it)
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
                viewModel.insertWord(it)
            }
        }

        //上一個狀態是新增題庫時, 進行選擇題庫的動作
        setFragmentResultListener("AddBookFragment"){_, _ ->
            if(viewModel.lastSearchQuery.isBlank())
                return@setFragmentResultListener

            findNavController().navigate(
                NavGraphDirections.actionGlobalDialogChooseBook(
                        "加入至題庫",
                        bookId = 0L
                )
            )
        }

        //可根據反白文字直接搜尋單字
        setSelectionTextCallback(object: SelectionTextCallback {
            override fun onSelectionTextChanged(text: String?) {
                if(text.isNullOrBlank())
                    return

                startSearch(query = text)
            }
        })

        viewModel.databaseEvent.observe(this){ pair->
            when(pair?.first){
                "insertWord" -> pair.second?.let { showInsertMessage(it) }
                "insertBook" -> "新增成功".showToast()
            }
        }

    }

    /**
     * 新增單字後顯示Snackbar, 可點擊檢視跳轉
     */
    private fun showInsertMessage(bundle: Bundle) {
        Snackbar.make(binding.root, "新增成功", Snackbar.LENGTH_LONG).run {
            showingSnackbar = this
            setAction("檢視") {
                findNavController().navigate(
                        NavGraphDirections.actionGlobalWordFragment(
                                bookId = bundle.getLong("bookId"),
                                wordId = bundle.getLong("wordId")
                        )
                )
            }
            setAnchorView(binding.extendedFab)
            show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showingSnackbar?.dismiss()
    }

    private fun setSelectionTextCallback(callback: SelectionTextCallback){
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
        viewModel.refreshClipboardText(requireActivity().getClipboardText())
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

            //前移hint的位置
            setPadding(-44, 0, 0, 0)

            setIconifiedByDefault(false)//展開SearchView

            //加入麥克風功能
            setSearchableInfo((requireActivity()
                    .getSystemService(Context.SEARCH_SERVICE) as SearchManager)
                    .getSearchableInfo(requireActivity().componentName))

            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if(hasFocus){
                    showSearchSuggestion()
                }
            }
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String) = false

                override fun onQueryTextChange(query: String): Boolean {

                    if (query.isNullOrBlank())
                        this@SearchFragment.refreshClipboardText()

                    query.trim().let {
                        viewModel.searchQuery.value = it
                        adapter.refreshSearchQuery(it)//for setting span
                    }

                    return false
                }
            })
        }

        /**
         * 只有在搜尋結果頁面才會導向其他頁並返回
         */
        if(viewModel.lastSearchQuery.isNotBlank()) {
            //回彈到此頁，從別的fragment回彈後要回復狀態
            searchView.setQuery(viewModel.lastSearchQuery, false)
            showResult(viewModel.wordLiveData.value)
            "ha".log()
        }
        else if(args.query.isNotEmpty()) {
            //初始化頁面
            startSearch(args.query)
        }
        else{
            //初始化頁面
            searchView.requestFocus()//加入光標
            openKeyboard()//開鍵盤
        }

    }

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
        binding.progressBar.visibility = View.VISIBLE
        searchView.setQuery(query, false)
        searchView.clearFocus()
        closeKeyboard()//顯示結果時關閉鍵盤
        viewModel.addOneSearchHistory(query.trim())//紀錄搜尋結果
        viewModel.getWordPage(query)
        if(!hasNetwork())
            "目前沒有網路連接".showToast()
    }

    private fun showResult(result: Result<Word>?) {
        binding.progressBar.isVisible = false
        binding.recyclerView.isVisible = false

        val word = result?.getOrNull()
        if(word != null)
            showWordCard(word)
        else
            showSearchNotFound()
    }


    //FAB會隨word-card一起出現, 且在建議列表出現時消失
    private fun showWordCard(word: Word) {
        resetUi(word)
        binding.wordCardItem.let {
            it.root.isVisible = true
            it.word = word
            it.wordInfo.text = "Dr.eye 譯典通"
        }
        binding.extendedFab.isVisible = true
    }

    private fun showSearchNotFound(){
        binding.wordCardItem.root.isVisible = false
        binding.extendedFab.isVisible = false
        binding.searchNotFound.root.isVisible = true
    }

    private fun showSearchSuggestion(){
        binding.wordCardItem.root.isVisible = false
        binding.extendedFab.isVisible = false
        binding.searchNotFound.root.isVisible = false
        binding.recyclerView.isVisible = true

    }



    /**
     * 結果頁(包含無搜尋結果的背景圖)的返回就是離開堆棧(題庫頁或其他)
     * 如果目前顯示搜尋建議列表, 且使用者第一次進入SearchFragment, 就同結果頁的行為,
     * 如果使用者不是第一次進入, 則應顯示上一次搜尋結果
     *
     * popBackStack會發送到消息隊列執行, 不是立即性的
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            if(binding.searchNotFound.root.isVisible ||
                    binding.wordCardItem.root.isVisible||
                    viewModel.lastSearchQuery.isNullOrBlank()
            ){
                findNavController().popBackStack()
                return@addCallback
            }

            searchView.setQuery(viewModel.lastSearchQuery, false)
            showResult(viewModel.wordLiveData.value)
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


    private fun setChildIsVisible(word: Word) {
        binding.wordCardItem.run {
            soundButton.isVisible = word.audioFilePath.isNotEmpty()
            translationTextView.isVisible = word.translation.isNotEmpty()
            variationTextView.isVisible = word.variation.isNotEmpty()
            exampleTextView.isVisible = word.example.isNotEmpty()
            synonymsTextView.isVisible = word.synonyms.isNotEmpty()
            noteTextView.isVisible = word.note.isNotEmpty()
        }
    }

    private fun resetUi(word: Word) {
        setChildIsVisible(word)
        resetExpandedState()
        binding.wordCardItem.scrollView.scrollTo(0, 0)
    }

    private fun resetExpandedState(){
        binding.wordCardItem.run {
            translationTextView.expand(false)
            variationTextView.expand(false)
            exampleTextView.expand(false)
            synonymsTextView.expand((false))
            noteTextView.expand(false)
        }
    }
}