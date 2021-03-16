package com.bu.selfstudy.ui.word

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.recyclerview.selection.Selection
import com.bu.selfstudy.logic.Repository
import com.bu.selfstudy.logic.model.Book
import com.bu.selfstudy.logic.model.Word

class WordViewModel : ViewModel(){
    var bookId = 0L
    var bookName = ""
    var searchQuery = ""

    var wordList = ArrayList<Word>()
    var wordListFiltered = ArrayList<Word>()
    var wordList2 = ArrayList<Word>()

    var selection: Selection<Long>? = null

    private val refreshLiveData = MutableLiveData<Long>()
    val wordListLiveData = Transformations.switchMap(refreshLiveData) { bookId ->
        Repository.loadWordsByBookId(bookId)
    }
    fun loadWordsByBookId(bookId: Long= this.bookId) {
        refreshLiveData.value = bookId
    }
}