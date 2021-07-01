package com.bu.selfstudy.data.repository

import androidx.lifecycle.liveData
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import com.bu.selfstudy.data.network.SelfStudyNetwork
import com.bu.selfstudy.tool.getSharedPreferences
import com.bu.selfstudy.tool.log
import com.bu.selfstudy.tool.setSharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import java.lang.Exception
import java.lang.RuntimeException
import javax.xml.parsers.SAXParserFactory


object WordRepository {
    private val wordDao = getDatabase().wordDao()

    fun loadWord(wordId: Long) = wordDao.loadWord(wordId)

    fun loadWords(bookId: Long, query: String) = wordDao.loadDistinctWords(bookId, query)
    fun loadWords(query: String) = wordDao.loadWords(query)

    fun loadWordTuplesWithPaging(bookId: Long, query: String) = wordDao.loadWordTuplesWithPaging(bookId, query)

    suspend fun getWord(wordName: String): Result<Word> = withContext(Dispatchers.IO){
        kotlin.runCatching {
            SelfStudyNetwork.getYahooWord(wordName)
        }
    }

    suspend fun insertWord(vararg word: Word) = withContext(Dispatchers.IO){
        wordDao.insert(*word).also {
            refreshBookSize()
        }
    }
    suspend fun updateWord(vararg word: Word) = withContext(Dispatchers.IO){
        wordDao.update(*word).also {
            refreshBookSize()
        }
    }

    suspend fun deleteWordToTrash(vararg wordId: Long) = withContext(Dispatchers.IO){
        wordDao.deleteWordToTrash(*wordId).also {
            refreshBookSize()
        }
    }

    suspend fun updateMarkWord(wordId:Long, isMark: Boolean) = withContext(Dispatchers.IO){
        wordDao.updateMarkWord(wordId, isMark)
    }

    suspend fun refreshBookSize(){
        GlobalScope.launch {
            BookRepository.refreshBookSize()
        }
    }
}