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
import kotlinx.coroutines.withContext

//search and result
object SearchRepository {
    private val searchHistoryDao = AppDatabase.getDatabase().searchHistoryDao()
    private val searchAutoCompleteDao = AppDatabase.getDatabase().searchAutoCompleteDao()

    fun loadSearchHistory(query: String) = searchHistoryDao.loadSearchHistory(query)
    fun loadSearchAutoComplete(query: String) = searchAutoCompleteDao.loadSearchAutoComplete(query)


    suspend fun insertSearchHistory(searchHistory: SearchHistory) = withContext(Dispatchers.IO){
        searchHistoryDao.insert(searchHistory)
    }
    suspend fun insertSearchAutoComplete(vararg searchAutoComplete: SearchAutoComplete) = withContext(Dispatchers.IO){
        searchAutoCompleteDao.insert(*searchAutoComplete)
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