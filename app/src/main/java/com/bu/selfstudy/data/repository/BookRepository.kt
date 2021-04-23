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

    suspend fun insertBook(vararg book: Book) = withContext(Dispatchers.IO){
        bookDao.insert(*book)
    }
    suspend fun updateBook(vararg book: Book) = withContext(Dispatchers.IO){
        bookDao.update(*book)
    }
    suspend fun deleteBook(vararg book: Book) = withContext(Dispatchers.IO){
        bookDao.delete(*book)
    }

}