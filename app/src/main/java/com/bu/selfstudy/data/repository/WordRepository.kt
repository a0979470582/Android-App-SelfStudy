package com.bu.selfstudy.data.repository

import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import com.bu.selfstudy.data.network.SelfStudyNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.xml.parsers.SAXParserFactory

object WordRepository {
    private val wordDao = getDatabase().wordDao()

    fun loadWord(wordId: Long) = wordDao.loadWord(wordId)

    fun loadWords(bookId: Long, query: String) = wordDao.loadDistinctWords(bookId, query)

    fun loadWordTuplesWithPaging(bookId: Long, query: String) = wordDao.loadWordTuplesWithPaging(bookId, query)

    suspend fun insertWord(vararg word: Word) = withContext(Dispatchers.IO){
        wordDao.insert(*word)
    }
    suspend fun updateWord(vararg word: Word) = withContext(Dispatchers.IO){
        wordDao.update(*word)
    }

    suspend fun updateMarkWord(wordId:Long, isMark: Boolean) = withContext(Dispatchers.IO){
        wordDao.updateMarkWord(wordId, isMark)
    }

    suspend fun deleteWordToTrash(vararg wordId: Long) = withContext(Dispatchers.IO){
        wordDao.deleteWordToTrash(*wordId)
    }

    suspend fun getWordPage(wordName: String) = withContext(Dispatchers.IO){
        SelfStudyNetwork.getWordPage(wordName)
    }
}