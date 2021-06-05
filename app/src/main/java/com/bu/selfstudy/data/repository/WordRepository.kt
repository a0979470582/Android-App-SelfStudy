package com.bu.selfstudy.data.repository

import androidx.lifecycle.liveData
import androidx.paging.toLiveData
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WordRepository {
    val wordDao = getDatabase().wordDao()

    fun loadWords(bookId: Long, query: String) = wordDao.loadDistinctWords(bookId, query)

    fun loadWord(wordId: Long) = wordDao.loadDistinctWord(wordId)

    fun loadWordsWithPaging(bookId: Long, query: String) = wordDao.loadWordsWithPaging(bookId, query)

    fun loadWordTuplesWithPaging(bookId: Long, query: String) = wordDao.loadWordTuplesWithPaging(bookId, query)


    suspend fun insertWord(vararg word: Word, bookId: Long) = withContext(Dispatchers.IO){
        wordDao.insert(*word).also { updateBookSize(bookId) }
    }
    suspend fun updateWord(vararg word: Word, bookId: Long) = withContext(Dispatchers.IO){
        wordDao.update(*word).also { updateBookSize(bookId) }
    }

    suspend fun updateMarkWord(wordId:Long, isMark: Boolean) = withContext(Dispatchers.IO){
        wordDao.updateMarkWord(wordId, isMark)
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