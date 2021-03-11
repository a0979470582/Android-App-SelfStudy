package com.bu.selfstudy.ui.book

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.bu.selfstudy.logic.Repository
import com.bu.selfstudy.logic.model.Book

class BookViewModel : ViewModel(){
    private val refreshLiveData = MutableLiveData<Int>()
    val bookListLiveData = Transformations.switchMap(refreshLiveData) {
        Repository.loadBooks()
    }

    fun loadBooks() {
        refreshLiveData.value = refreshLiveData.value
    }
    val bookList = ArrayList<Book>()
}