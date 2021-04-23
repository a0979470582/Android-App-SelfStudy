package com.bu.selfstudy.data.repository

import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Member
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WordRepository {
    private val wordDao = getDatabase().wordDao()

    fun loadWords(bookId: Long, query: String) =
        wordDao.loadWordsDistinctUntilChanged(bookId, query)

    suspend fun insertWords(vararg word: Word) = withContext(Dispatchers.IO){
        wordDao.insert(*word)
    }
    suspend fun updateWords(vararg word: Word) = withContext(Dispatchers.IO){
        wordDao.update(*word)
    }
    suspend fun deleteWords(vararg word: Word) = withContext(Dispatchers.IO){
        wordDao.delete(*word)
    }
}