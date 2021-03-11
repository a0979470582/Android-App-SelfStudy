package com.bu.selfstudy.logic

import androidx.lifecycle.liveData
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.logic.model.Book
import com.bu.selfstudy.logic.model.Word
import kotlinx.coroutines.Dispatchers
import com.bu.selfstudy.logic.room.AppDatabase.Companion.getDatabase
import kotlin.coroutines.CoroutineContext

object Repository {
    fun loadBooks() = fire(Dispatchers.IO) {
        val books = getDatabase().bookDao().loadBooksByMemberId()
        Result.success(books)
        //Result.failure(RuntimeException("books is ${books}"))
    }


    fun loadWordsByBookId(bookId: Long)= fire(Dispatchers.IO) {
        val words = getDatabase().wordDao().loadWordsByBookId(bookId)
        Result.success(words)
        //Result.failure(RuntimeException("books is ${books}"))
    }


    private fun <T> fire(context: CoroutineContext, block: suspend ()->Result<T>) = liveData(context){
        val result = try{
            block()
        }catch (e: Exception){
            Result.failure<T>(e)
        }
        emit(result)
    }
}