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

    suspend fun insertWords(words: List<Word>) = withContext(Dispatchers.IO){
        wordDao.insert(*words.toTypedArray())
    }
    suspend fun updateWords(words: List<Word>) = withContext(Dispatchers.IO){
        wordDao.update(*words.toTypedArray())
    }
    suspend fun deleteWords(words: List<Word>) = withContext(Dispatchers.IO){
        wordDao.delete(*words.toTypedArray())
    }
}