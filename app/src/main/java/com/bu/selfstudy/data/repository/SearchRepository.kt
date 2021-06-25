package com.bu.selfstudy.data.repository

import com.bu.selfstudy.data.AppDatabase
import com.bu.selfstudy.data.dao.SearchHistoryDao
import com.bu.selfstudy.data.model.SearchAutoComplete
import com.bu.selfstudy.data.model.SearchHistory
import com.bu.selfstudy.data.model.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//search and result
object SearchRepository {
    private val searchHistoryDao = AppDatabase.getDatabase().searchHistoryDao()
    private val searchAutoCompleteDao = AppDatabase.getDatabase().searchAutoCompleteDao()

    fun loadSearchHistory(query: String) = searchHistoryDao.loadSearchHistory(query)
    fun loadSearchAutoComplete(query: String) = searchAutoCompleteDao.loadSearchAutoComplete(query)

    suspend fun insertSearchHistory(vararg searchHistory: SearchHistory) = withContext(Dispatchers.IO){
        searchHistoryDao.insert(*searchHistory)
    }
    suspend fun insertSearchAutoComplete(vararg searchAutoComplete: SearchAutoComplete) = withContext(Dispatchers.IO){
        searchAutoCompleteDao.insert(*searchAutoComplete)
    }
}