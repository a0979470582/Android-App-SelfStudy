package com.bu.selfstudy.ui.book

import android.os.Bundle
import androidx.lifecycle.*
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.tool.SingleLiveData
import com.bu.selfstudy.tool.putBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BookViewModel : ViewModel(){
    val databaseEvent = SingleLiveData<Pair<String, Bundle?>>()

    fun calculateBookSize(){
        viewModelScope.launch {
            BookRepository.updateBookSize()
        }
    }

    var longPressedBook: Book? = null
    fun refreshLongPressedBook(bookList: List<Book>, bookId: Long){
        viewModelScope.launch(Dispatchers.Default) {
            longPressedBook = bookList.firstOrNull{ it.id==bookId }
        }
    }

    fun deleteBook(bookId: Long, bookName: String){
        viewModelScope.launch {
            if(BookRepository.updateBookIsTrash(bookId, true) > 0)
                databaseEvent.postValue("delete" to putBundle("bookName", bookName))
        }
    }

    fun insertBook(bookName: String){
        viewModelScope.launch {
            val book = Book(bookName = bookName, memberId = SelfStudyApplication.memberId)
            if(BookRepository.insertBook(book).isNotEmpty()) {
                databaseEvent.postValue("insertBook" to null)
            }
        }
    }

    fun updateBook(book: Book){
        viewModelScope.launch{
            if(BookRepository.updateBook(book)>0) {
                databaseEvent.postValue("update" to null)
            }
        }
    }

}