package com.bu.selfstudy.ui.book

import android.os.Bundle
import androidx.lifecycle.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.tool.SingleLiveData
import com.bu.selfstudy.tool.putBundle
import kotlinx.coroutines.launch


class BookViewModel : ViewModel(){
    val databaseEvent = SingleLiveData<Pair<String, Bundle?>>()

    val bookListLiveData = BookRepository.loadBooks().asLiveData()


    fun calculateBookSize(){
        viewModelScope.launch {
            BookRepository.updateBookSize()
        }
    }

    var chosenBook: Book? = null


    fun editBook(bookName:String, explanation:String){
        viewModelScope.launch{
            chosenBook?.copy()?.let {
                it.bookName = bookName
                it.explanation = explanation
                if(BookRepository.updateBook(it)>0) {
                    databaseEvent.postValue("update" to null)
                }
            }
        }
    }

    fun deleteBook(){
        viewModelScope.launch {
            chosenBook?.let {
                if(BookRepository.delete(it.id) > 0)
                    databaseEvent.postValue("delete" to putBundle("bookName", it.bookName))
            }
        }
    }

    fun archiveBook(isArchive: Boolean){
        viewModelScope.launch {
            chosenBook?.let {
                if(BookRepository.updateIsArchive(it.id, isArchive) > 0)
                    databaseEvent.postValue("archive" to putBundle("bookName", it.bookName))
            }
        }
    }

    fun updateBookColor(colorInt: Int) {
        viewModelScope.launch {
            chosenBook?.let {
                BookRepository.updateColor(it.id, colorInt)
            }
        }
    }

}