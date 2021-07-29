package com.bu.selfstudy.ui.search

import android.app.SearchManager
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bu.selfstudy.R
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.SearchHistory
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.FragmentSearchBinding
import com.bu.selfstudy.tool.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private val adapter = SuggestionListAdapter(this)
    private val viewModel: SearchViewModel by viewModels()

    private lateinit var searchView: SearchView

    private var mediaPlayer: MediaPlayer? = null

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_toolbar, menu)

        searchView = menu.findItem(R.id.action_search).actionView as SearchView

        with(searchView){

            setPadding(-44,0,0,0)//修改hint的位置

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

                    if(query.isNullOrBlank())
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
        searchView.clearFocus()//顯示結果時關閉鍵盤
        viewModel.addOneSearchHistory(query.trim())//紀錄搜尋結果
        viewModel.getWordPage(query)
        if(!hasNetwork())
            "目前沒有網路連接".showToast()
    }


    private fun showWordCard(result: Result<Word>) {
        val word = result.getOrNull()
        binding.progressBar.visibility = View.INVISIBLE
        binding.recyclerView.visibility = View.INVISIBLE
        if(word != null){
            binding.wordCardItem.let {
                it.root.visibility = View.VISIBLE
                it.word = word
            }
        }else{
            binding.searchNotFound.root.visibility = View.VISIBLE
        }
    }
    private fun showSearchSuggestion(){
        binding.recyclerView.visibility = View.VISIBLE
        binding.wordCardItem.root.visibility = View.INVISIBLE
        binding.searchNotFound.root.visibility = View.INVISIBLE
    }



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

}