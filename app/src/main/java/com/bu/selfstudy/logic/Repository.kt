package com.bu.selfstudy.logic

import com.bu.selfstudy.logic.model.Word
import com.bu.selfstudy.logic.room.AppDatabase.Companion.getDatabase

object Repository {
    private val wordDao = getDatabase().wordDao()
    private val bookDao = getDatabase().bookDao()

    fun loadBooks() = bookDao.loadBooksByMemberId()

    fun loadWords(bookId: Long, query: String) = wordDao.loadWords(bookId, query)

    suspend fun insertWords(words: List<Word>) = wordDao.insertWords(*words.toTypedArray())
    suspend fun updateWords(words: List<Word>) { wordDao.updateWords(*words.toTypedArray())}
    suspend fun deleteWords(words: List<Word>) = wordDao.deleteWords(*words.toTypedArray())
}