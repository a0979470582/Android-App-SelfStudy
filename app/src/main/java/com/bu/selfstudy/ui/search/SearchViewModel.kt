package com.bu.selfstudy.ui.search

import android.os.Bundle
import androidx.lifecycle.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.SearchHistory
import com.bu.selfstudy.data.model.SearchRow
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.SearchRepository
import com.bu.selfstudy.data.repository.WordRepository
import com.bu.selfstudy.tool.SingleLiveData
import com.bu.selfstudy.tool.log
import com.bu.selfstudy.tool.putBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    val databaseEvent = SingleLiveData<Pair<String, Bundle?>>()

    var clipboardText: String? = null
    var lastSearchQuery: String = ""

    val wordLiveData = MutableLiveData<Result<Word>>()

    //給予初始值(空字串也可)就能觸發switchMap, 否則不觸發, 需設值觸發
    val searchQuery = MutableLiveData<String>()

    private val autoCompleteList = searchQuery.switchMap {
        SearchRepository.loadAutoComplete(it).asLiveData()
    }
    private val historyList = searchQuery.switchMap {
        SearchRepository.loadHistory(it).asLiveData()
    }

    val suggestionList = MediatorLiveData<List<SearchRow>>().also {
        it.addSource(autoCompleteList) { combineList() }
        it.addSource(historyList) { combineList() }
    }

    /**
     * 合併剪貼簿內容+歷史紀錄+自動完成, 使用者已輸入query時, 隱藏剪貼簿內容,
     * 未輸入query時, 隱藏自動完成
     */
    private fun combineList() {
        viewModelScope.launch(Dispatchers.Default) {

            val newList = ArrayList<SearchRow>()

            if(searchQuery.value!!.isNullOrBlank()){
                clipboardText?.let{
                    newList.add(SearchRow(searchName = it))
                }
            }
            historyList.value?.let {
                newList.addAll(it)
            }
            autoCompleteList.value?.let {
                if(searchQuery.value!!.isNotBlank())
                    newList.addAll(it)
            }

            suggestionList.postValue(newList)
        }
    }

    fun refreshClipboardText(text: String?){
        clipboardText = checkClipboardText(text)
    }

    private fun checkClipboardText(text: String?): String?{
        if(text.isNullOrBlank() || text.length > 25)
            return null

        return Regex("[a-zA-Z]{1,25}").find(text)?.value
    }

    fun addOneSearchHistory(searchName: String) {
        viewModelScope.launch {
            SearchRepository.insertHistory(searchName)
        }
    }

    fun removeSearchHistory(searchHistory: SearchHistory) {
        viewModelScope.launch {
            SearchRepository.deleteHistory(searchHistory)
        }
    }

    fun getWordPage(wordName: String) {
        viewModelScope.launch {
            lastSearchQuery = wordName
            wordLiveData.value = WordRepository.getYahooWord(wordName)
        }
    }

    fun insertWord(word: Word){
        viewModelScope.launch(Dispatchers.IO) {
            val resultIdList = WordRepository.insertWord(word)
            if(resultIdList.isNotEmpty())
                databaseEvent.postValue("insertWord" to
                        putBundle("wordId", resultIdList.first())
                                .putBundle("bookId", word.bookId))
        }
    }
    fun insertBook(book: Book){
        viewModelScope.launch {
            if(BookRepository.insertBook(book).isNotEmpty()) {
                databaseEvent.postValue("insertBook" to null)
            }
        }
    }
}