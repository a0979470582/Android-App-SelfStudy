package com.bu.selfstudy.data.repository

import com.bu.selfstudy.data.AppDatabase
import com.bu.selfstudy.data.dao.SearchAutoCompleteDao
import com.bu.selfstudy.data.dao.SearchHistoryDao
import com.bu.selfstudy.data.local.LoadSearchAutoComplete
import com.bu.selfstudy.data.model.SearchAutoComplete
import com.bu.selfstudy.data.model.SearchHistory
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.tool.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

//search and result
object SearchRepository {
    private val searchHistoryDao = AppDatabase.getDatabase().searchHistoryDao()
    private val searchAutoCompleteDao = AppDatabase.getDatabase().searchAutoCompleteDao()

    fun loadSearchHistory(query: String) = searchHistoryDao.loadSearchHistory(query)
    fun loadSearchAutoComplete(query: String) = searchAutoCompleteDao.loadSearchAutoComplete(query)


    suspend fun insertSearchHistory(searchHistory: SearchHistory) = withContext(Dispatchers.IO){
        val resultId = searchHistoryDao.insertSearchHistory(searchHistory)
        searchAutoCompleteDao.setIsHistory(searchHistory.searchName, isHistory = true)
        resultId
    }
    suspend fun insertSearchAutoComplete(vararg searchAutoComplete: SearchAutoComplete) = withContext(Dispatchers.IO){
        searchAutoCompleteDao.insert(*searchAutoComplete)
    }

    suspend fun clearSearchHistory() = withContext(Dispatchers.IO){
        val searchHistoryList  = searchHistoryDao.loadSearchHistory("").first()
        searchAutoCompleteDao.setIsHistory(
                searchName = searchHistoryList.map { it.searchName }.toTypedArray(),
                isHistory = false
        )
        searchHistoryDao.delete(*searchHistoryList.toTypedArray())
    }

    suspend fun insertLocalAutoComplete() = withContext(Dispatchers.IO){
        for(filename in LoadSearchAutoComplete.filenameList){
            val autoCompleteList = LoadSearchAutoComplete.loadData(filename).map {
                SearchAutoComplete(searchName = it)
            }
            searchAutoCompleteDao.insertSearchAutoComplete(*autoCompleteList.toTypedArray())
        }
    }
    suspend fun removeLocalAutoComplete() = withContext(Dispatchers.IO){
        searchAutoCompleteDao.loadSearchAutoComplete("").collect {
            it.forEach { row->
                row.searchName.forEach { char->
                    val commonLetter = char.isLowerCase()
                    if(!commonLetter)
                        searchAutoCompleteDao.delete(row)
                }
            }
        }
    }
}