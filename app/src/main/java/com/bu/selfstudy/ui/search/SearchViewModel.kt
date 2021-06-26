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
    //val clipboardText = MutableLiveData<String>()
    val wordLiveData = MutableLiveData<Result<Word>>()


    val searchQuery = MutableLiveData<String>()
    private val autoCompleteList = searchQuery.switchMap {
        SearchRepository.loadSearchAutoComplete(it).asLiveData()
    }
    private val historyList = searchQuery.switchMap {
        SearchRepository.loadSearchHistory(it).asLiveData()
    }

    val suggestionList = MediatorLiveData<List<SearchRow>>().also {
        it.addSource(autoCompleteList) { combineList() }
        it.addSource(historyList) { combineList() }
    }

    @Synchronized
    private fun combineList() {
        viewModelScope.launch(Dispatchers.Default) {
            val newList = ArrayList<SearchRow>()

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

    fun addOneSearchHistory(query: String) {
        viewModelScope.launch {
            val searchHistoryObj = SearchHistory(searchName=query)
            SearchRepository.insertSearchHistory(searchHistoryObj)
        }
    }


    fun getWordPage(wordName: String) {
        viewModelScope.launch {
            wordLiveData.value = WordRepository.getWord(wordName)
        }
    }

}