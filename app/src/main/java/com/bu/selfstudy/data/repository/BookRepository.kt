package com.bu.selfstudy.data.repository

import com.bu.selfstudy.R
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import com.bu.selfstudy.data.local.LoadLocalBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

//use coroutine, try-catch, don't write many code here
object BookRepository {
    private val bookDao = getDatabase().bookDao()


    //load
    fun loadOneBook(bookId: Long) = bookDao.loadOneBook(bookId)

    fun loadBooks() = bookDao.loadBooks()

    fun loadBooksArchive() = bookDao.loadBooksArchived()

    suspend fun loadLocalBookNames() = LoadLocalBook.loadNames()


    //insert
    suspend fun insertBook(book: Book) = withContext(Dispatchers.IO){

        return@withContext bookDao.insert(book).also { idList->

            if(book.colorInt == 0){
                val intArray = SelfStudyApplication.context.resources
                    .getIntArray(R.array.book_color_list)

                bookDao.updateColorInt(idList[0], intArray[idList[0].toInt() % intArray.size])
            }
        }
    }


    suspend fun insertLocalBook(bookName: String,explanation: String, file: File) = withContext(Dispatchers.IO) {
        val bookId = insertBook(
            Book(
                memberId = SelfStudyApplication.memberId,
                bookName = bookName,
                explanation = explanation
            )
        )[0]

        LoadLocalBook.loadBookData(file).let { wordList->
            wordList.forEach { it.bookId = bookId }

            WordRepository.insertWord(*wordList.toTypedArray()).also { idList->
                updateBookSize(bookId = bookId)
                file.delete()
                return@withContext idList.isNotEmpty()
            }
        }

        return@withContext false
    }

    //update
    suspend fun updateBook(vararg book: Book) = withContext(Dispatchers.IO){
        bookDao.update(*book)
    }

    suspend fun updatePosition(bookId: Long, position: Int) = withContext(Dispatchers.IO){
        bookDao.updatePosition(bookId, position)
    }

    suspend fun updateBookSize(bookId: Long = 0L) = withContext(Dispatchers.IO){
        if(bookId > 0L)
            bookDao.updateSize(bookId)
        else
            bookDao.updateSizeAllBook()
    }

    suspend fun updateIsArchive(bookId: Long, isArchive: Boolean) = withContext(Dispatchers.IO){
        bookDao.updateIsArchive(bookId, isArchive)
    }

    suspend fun updateColor(bookId: Long, colorInt: Int) {
        bookDao.updateColorInt(bookId, colorInt)
    }

    //delete
    suspend fun delete(bookId: Long) = withContext(Dispatchers.IO){
        bookDao.delete(bookId)
    }
}