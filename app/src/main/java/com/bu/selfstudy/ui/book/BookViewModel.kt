package com.bu.selfstudy.ui.book

import androidx.lifecycle.*
import com.bu.selfstudy.data.model.Book


class BookViewModel : ViewModel(){


}

/*
val searchQueryLD = MutableLiveData("")
private val refreshLD = MutableLiveData<Int>(1)
val bookListLD= refreshLD.switchMap{
    BookRepository.loadBooks().asLiveData()
}
val idList = ArrayList<Long>()

var isLoadingLD = MutableLiveData(false)
val databaseEventLD = MutableLiveData<DatabaseResultEvent>()

fun refresh() {
    refreshLD.value = refreshLD.value
}

fun insertBooks(books:List<Book>){
    setDatabaseResult(isLoadingLD, databaseEventLD, "新增"){
        BookRepository.insertBook(*books.toTypedArray()).size
    }
}

fun deleteBooksToTrash(bookIds: List<Long>){
    setDatabaseResult(isLoadingLD, databaseEventLD, "刪除"){
        BookRepository.updateBook(
                *getBookList(bookIds).onEach { it.isTrash = true }.toTypedArray()
        )
    }
}

fun insertWords(words:List<Word>){
    setDatabaseResult(isLoadingLD, databaseEventLD, "新增"){
        WordRepository.insertWord(*words.toTypedArray()).size
    }
}

private fun getBookList(bookIds: List<Long>) = bookListLD.value!!.filter { bookIds.contains(it.id) }
*/