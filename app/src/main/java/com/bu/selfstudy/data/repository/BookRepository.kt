package com.bu.selfstudy.data.repository

import com.bu.selfstudy.R
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import com.bu.selfstudy.data.local.LoadLocalBook
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//use coroutine, try-catch, don't write many code here
object BookRepository {
    private val bookDao = getDatabase().bookDao()
    private val wordDao = getDatabase().wordDao()

    fun loadBooks(memberId:Long=SelfStudyApplication.memberId) =
            bookDao.loadDistinctBooks(memberId)

    suspend fun loadLocalBookNames() = LoadLocalBook.loadNames()

    suspend fun insertBook(book: Book) = withContext(Dispatchers.IO){
        if(book.colorInt == 0){
            book.colorInt = SelfStudyApplication.context.resources.getIntArray(
                    R.array.book_color_list).random()
        }
        bookDao.insert(book)
    }


    suspend fun insertLocalBook(bookName: String) = withContext(Dispatchers.IO) {
        val bookId = insertBook(Book(
                memberId = SelfStudyApplication.memberId,
                bookName = bookName))[0]

        val wordList = LoadLocalBook.loadBookData(bookName)

        wordList.forEach { it.bookId = bookId }

        val wordIdList = WordRepository.insertWord(*wordList.toTypedArray())
        return@withContext if(wordIdList.isNotEmpty())
            "新增成功"
        else
            "新增失敗"
    }

    suspend fun updateBook(vararg book: Book) = withContext(Dispatchers.IO){
        bookDao.update(*book)
    }
    suspend fun updateBookPosition(bookId: Long, position: Int) = withContext(Dispatchers.IO){
        bookDao.updateBookPosition(bookId, position)
    }

    suspend fun updateBookSize() = withContext(Dispatchers.IO){
        bookDao.updateBookSize()
    }

    suspend fun updateBookIsTrash(bookId: Long, isTrash: Boolean) = withContext(Dispatchers.IO){
        bookDao.updateBookIsTrash(bookId, isTrash).also {
            wordDao.updateWordIsTrash(bookId, isTrash)
        }
    }
}