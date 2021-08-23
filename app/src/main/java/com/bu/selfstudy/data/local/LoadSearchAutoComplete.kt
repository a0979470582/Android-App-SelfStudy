package com.bu.selfstudy.data.local

import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.SearchAutoComplete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LoadSearchAutoComplete {
    suspend fun loadData(fileName: String): List<SearchAutoComplete> = withContext(Dispatchers.IO){
        val inputStream = SelfStudyApplication.context.assets.open(fileName)
        val rowList = inputStream.reader().readLines()
        return@withContext rowList.map { SearchAutoComplete(searchName = it) }
    }
}