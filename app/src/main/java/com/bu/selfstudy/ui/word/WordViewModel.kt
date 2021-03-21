package com.bu.selfstudy.ui.word

import android.util.Log
import androidx.lifecycle.*
import androidx.recyclerview.selection.Selection
import com.bu.selfstudy.logic.Repository
import com.bu.selfstudy.logic.model.Book
import com.bu.selfstudy.logic.model.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale.filter

class WordViewModel(
        private val savedStateHandle: SavedStateHandle,
        val bookId:Long,
        val bookName:String
    ) : ViewModel(){

    val searchQuery: MutableLiveData<String> = MutableLiveData("")
    val wordList: LiveData<List<Word>> = searchQuery.switchMap{
        val query = if(it.isBlank()){
            "%"
        }else {
            "%$it%"
        }
        Repository.loadWords(bookId, query).asLiveData()
    }
    fun deleteWords(ids: List<Long>){
        viewModelScope.launch{
            val newWordList:List<Word> = (wordList as ArrayList<Word>)
                    .filter { ids.contains(it.id) }
                    .onEach {it.isTrash=true}
            Repository.updateWords(newWordList)
        }
    }

    fun copyWordsTo(ids: List<Long>, bookId: Long){
        //傳id陣列去DB, 透過資料庫底層來實現重複插入
        viewModelScope.launch {
            //Repository.insert(words)
        }
    }

    //傳id陣列去DB, 將每一項的bookId設為新值, 透過Flow和LiveData自動返回
    fun moveWordsTo(ids: List<Long>, bookId: Long){
        viewModelScope.launch {
            //Repository.deleteWords(words)
        }
    }
}