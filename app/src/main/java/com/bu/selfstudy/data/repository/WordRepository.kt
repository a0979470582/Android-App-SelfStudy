package com.bu.selfstudy.data.repository

import androidx.lifecycle.liveData
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import com.bu.selfstudy.tool.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object WordRepository {
    val wordDao = getDatabase().wordDao()

    fun loadWords(bookId: Long, query: String) = wordDao.loadDistinctWords(bookId, query)

    fun loadWord(wordId: Long) = wordDao.loadDistinctWord(wordId)

    suspend fun insertWord(vararg word: Word, bookId: Long) = withContext(Dispatchers.IO){
        wordDao.insert(*word).also { updateBookSize(bookId) }
    }
    suspend fun updateWord(vararg word: Word, bookId: Long) = withContext(Dispatchers.IO){
        wordDao.update(*word).also { updateBookSize(bookId) }
    }
    suspend fun deleteWord(vararg wordId: Long, bookId: Long) = withContext(Dispatchers.IO){
        wordDao.delete(*wordId).also { updateBookSize(bookId) }
    }
    suspend fun deleteWordToTrash(vararg wordId: Long, bookId: Long) = withContext(Dispatchers.IO){
        wordDao.deleteWordToTrash(*wordId).also { updateBookSize(bookId) }
    }
    suspend fun deleteOneBookToTrash(bookId: Long) = withContext(Dispatchers.IO){
        wordDao.deleteOneBookToTrash(bookId).also { updateBookSize(bookId) }
    }
    suspend fun updateBookSize(bookId: Long){
        BookRepository.updateBookSize(bookId)
    }
}