package com.bu.selfstudy.ui.word

import android.util.Log
import androidx.lifecycle.*
import com.bu.selfstudy.logic.Repository
import com.bu.selfstudy.logic.model.Book
import com.bu.selfstudy.logic.model.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WordViewModel(val bookId:Long, val bookName:String ) : ViewModel(){
    val searchQueryLD: MutableLiveData<String> = MutableLiveData("")
    val wordListLD: LiveData<List<Word>> = searchQueryLD.switchMap{
        val query = if(it.isBlank()) "%" else "%$it%"
        Repository.loadWords(bookId, query).asLiveData()
    }
    //觸發第一次取值, 想一下bookList的架構
    val bookListLD: LiveData<List<Book>> = Repository.loadBooks().asLiveData()

    fun deleteWords(ids: List<Long>){
        viewModelScope.launch(Dispatchers.IO){
            Repository.updateWords(getWordList(ids).onEach { it.isTrash = true })
        }
    }

    fun copyWordsTo(ids: List<Long>, bookId: Long){
        viewModelScope.launch(Dispatchers.IO){
            Repository.insertWords(getWordList(ids).onEach {
                it.id = 0
                it.bookId = bookId
            })
        }
    }

    fun moveWordsTo(ids: List<Long>, bookId: Long){
        viewModelScope.launch(Dispatchers.IO){
            Repository.updateWords(getWordList(ids).onEach { it.bookId = bookId })
        }
    }

    private fun getWordList(wordIds: List<Long>) = wordListLD.value!!.filter { wordIds.contains(it.id) }


    companion object {
        fun provideFactory(bookId:Long, bookName:String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return WordViewModel(bookId, bookName) as T
            }
        }
    }
}