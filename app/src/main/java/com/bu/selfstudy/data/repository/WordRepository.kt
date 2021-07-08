package com.bu.selfstudy.data.repository

import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import com.bu.selfstudy.data.network.SelfStudyNetwork
import kotlinx.coroutines.*


object WordRepository {
    private val wordDao = getDatabase().wordDao()

    //get a word
    fun loadWord(wordId: Long) = wordDao.loadWord(wordId)

    //find word
    fun loadWords(query: String) = wordDao.loadWords(query)

    //find word in one book
    fun loadWords(bookId: Long, query: String) = wordDao.loadDistinctWords(bookId, query)

    //paging3
    fun loadWordTuplesWithPaging(bookId: Long, query: String) =
            wordDao.loadWordTuplesWithPaging(bookId, query)

    //connect yahoo
    suspend fun getWord(wordName: String): Result<Word> = withContext(Dispatchers.IO){
        kotlin.runCatching {
            SelfStudyNetwork.getYahooWord(wordName)
        }
    }

    //insert
    suspend fun insertWord(vararg word: Word) = withContext(Dispatchers.IO){
        wordDao.insert(*word)
    }

    //update
    suspend fun updateWord(vararg word: Word) = withContext(Dispatchers.IO){
        wordDao.update(*word)
    }

    suspend fun updateWordIsTrash(vararg wordId: Long, isTrash: Boolean) = withContext(Dispatchers.IO){
        wordDao.updateWordIsTrash(wordId = *wordId, isTrash)
    }

    suspend fun updateWordMark(wordId:Long, isMark: Boolean) = withContext(Dispatchers.IO){
        wordDao.updateWordMark(wordId, isMark)
    }
}