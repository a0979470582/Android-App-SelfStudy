package com.bu.selfstudy.data.repository

import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Member
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BookRepository {
    private val bookDao = getDatabase().bookDao()

    fun loadBooks(memberId:Long=SelfStudyApplication.memberId) = bookDao.loadDistinctBooks(memberId)

    suspend fun insertBooks(books: List<Book>) = withContext(Dispatchers.IO){
        bookDao.insert(*books.toTypedArray())
    }
    suspend fun updateBooks(books: List<Book>) = withContext(Dispatchers.IO){
        bookDao.update(*books.toTypedArray())
    }
    suspend fun deleteBooks(books: List<Book>) = withContext(Dispatchers.IO){
        bookDao.delete(*books.toTypedArray())
    }

}