package com.bu.selfstudy.data.repository

import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import com.bu.selfstudy.data.model.RecentWord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

object RecentWordRepository {
    private val recentWordDao = getDatabase().recentWordDao()
    private val wordDao = getDatabase().wordDao()
    private val bookDao = getDatabase().bookDao()


    fun loadRecentWord() = recentWordDao.loadRecentWord()

    suspend fun insertRecentWord(recentWord: RecentWord) = withContext(Dispatchers.IO){
        recentWordDao.insertRecentWord(recentWord)
    }

    suspend fun refreshRecentWord(recentWord: RecentWord) = withContext(Dispatchers.Default){
        val word = wordDao.loadOneWord(recentWord.wordId).firstOrNull()

        if(word == null) {
            recentWordDao.delete(recentWord)
            return@withContext
        }

        val book = bookDao.loadOneBook(word.bookId).firstOrNull()
        if(book == null ) {
            recentWordDao.delete(recentWord)
            return@withContext
        }

        if( recentWord.wordName==word.wordName &&
            recentWord.bookId==book.id &&
            recentWord.bookName==book.bookName
        )
            return@withContext
        else
            recentWordDao.update(recentWord.copy().also {
                it.wordName = word.wordName
                it.bookId = book.id
                it.bookName = book.bookName
            })
    }

}