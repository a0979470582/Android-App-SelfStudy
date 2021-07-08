package com.bu.selfstudy.ui.search

import androidx.lifecycle.*
import com.bu.selfstudy.data.model.SearchHistory
import com.bu.selfstudy.data.model.SearchRow
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.repository.SearchRepository
import com.bu.selfstudy.data.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    var clipboardText: String? = null
    val wordLiveData = MutableLiveData<Result<Word>>()


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
        clipboardText = text
    }

    fun addOneSearchHistory(query: String) {
        viewModelScope.launch {
            val searchHistoryObj = SearchHistory(searchName=query)
            SearchRepository.insertHistory(searchHistoryObj)
        }
    }

    fun removeSearchHistory(searchHistory: SearchHistory) {
        viewModelScope.launch {
            SearchRepository.deleteHistory(searchHistory)

        }
    }

    fun getWordPage(wordName: String) {
        viewModelScope.launch {
            wordLiveData.value = WordRepository.getWord(wordName)
        }
    }


}