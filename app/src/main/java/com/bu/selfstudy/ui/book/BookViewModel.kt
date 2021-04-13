package com.bu.selfstudy.ui.book

import androidx.lifecycle.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.tools.DatabaseResultEvent
import com.bu.selfstudy.tools.setDatabaseResult


class BookViewModel : ViewModel(){}
/*
val searchQueryLD = MutableLiveData("")
private val refreshLD = MutableLiveData<Int>(1)
val bookListLD= refreshLD.switchMap{
    Repository.loadBooks().asLiveData()
}
val idList = ArrayList<Long>()

var isLoadingLD = MutableLiveData(false)
val databaseEventLD = MutableLiveData<DatabaseResultEvent>()

fun refresh() {
    refreshLD.value = refreshLD.value
}

fun insertBooks(books:List<Book>){
    setDatabaseResult(isLoadingLD, databaseEventLD, "新增"){
        Repository.insertBooks(books).size
    }
}

fun deleteBooksToTrash(bookIds: List<Long>){
    setDatabaseResult(isLoadingLD, databaseEventLD, "刪除"){
        Repository.updateBooks(
                getBookList(bookIds).onEach { it.isTrash = true }
        )
    }
}

fun insertWords(words:List<Word>){
    setDatabaseResult(isLoadingLD, databaseEventLD, "新增"){
        Repository.insertWords(words).size
    }
}

fun setIdList(books: List<Book>) {
    idList.clear()
    idList.addAll(books.map { it.id })
}

private fun getBookList(bookIds: List<Long>) = bookListLD.value!!.filter { bookIds.contains(it.id) }
}*/