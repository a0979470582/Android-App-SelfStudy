package com.bu.selfstudy.ui.word

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.bu.selfstudy.logic.Repository
import com.bu.selfstudy.logic.model.Book
import com.bu.selfstudy.logic.model.Word

class WordViewModel : ViewModel(){
    private val refreshLiveData = MutableLiveData<Long>()
    val wordListLiveData = Transformations.switchMap(refreshLiveData) { bookId ->
        Repository.loadWordsByBookId(bookId)
    }

    fun loadWordsByBookId(bookId: Long= this.bookId) {
        refreshLiveData.value = bookId
    }
    val wordList = ArrayList<Word>()
    var bookId = 0L
    var bookName = ""
}