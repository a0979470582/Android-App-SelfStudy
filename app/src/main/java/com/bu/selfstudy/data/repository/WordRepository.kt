package com.bu.selfstudy.data.repository

import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WordRepository {
    private val wordDao = getDatabase().wordDao()

    fun loadWords(bookId: Long, query: String) =
        wordDao.loadDistinctWords(bookId, query)

    fun loadWord(wordId: Long) = wordDao.loadDistinctWord(wordId)

    suspend fun insertWord(vararg word: Word) = withContext(Dispatchers.IO){
        wordDao.insert(*word)
    }
    suspend fun updateWord(vararg word: Word) = withContext(Dispatchers.IO){
        wordDao.update(*word)
    }
    suspend fun deleteWord(vararg wordId: Long) = withContext(Dispatchers.IO){
        wordDao.delete(*wordId)
    }
    suspend fun deleteWordToTrash(vararg wordId: Long) = withContext(Dispatchers.IO){
        wordDao.deleteWordToTrash(*wordId)
    }

}