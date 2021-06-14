  package com.bu.selfstudy.ui.wordcard


import android.os.Bundle
import androidx.lifecycle.*
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.WordRepository
import com.bu.selfstudy.tool.SingleLiveData
import com.bu.selfstudy.tool.putBundle
import com.bu.selfstudy.ui.editword.EditWordViewModel
import com.bu.selfstudy.ui.wordlist.WordListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/**
 * 更改資料庫中的wordList都會透過LiveData反應在目前的ViewPager, 例如新增,刪除,修改都會同步到資料庫,
 * 接著透過LiveData傳回並在ViewPager看到更新, 而wordFragment的生命週期進入onStop時, 會同步目前使用者所
 * 看到的單字頁(wordId)到資料庫
 */
class WordCardViewModel(val currentOpenBook: Book) : ViewModel() {

    val wordListLiveData = WordRepository.loadWords(currentOpenBook.id, "%").asLiveData()

    var firstLoad = true

    val databaseEvent = SingleLiveData<Pair<String, Bundle?>>()

    //將當前頁面同步到資料庫
    var currentOpenWord: Word? = null
    var currentPosition: Int? = null
    val isMarkLiveData = MutableLiveData(false)
    fun updateCurrentPosition(realPosition: Int){
        viewModelScope.launch(Dispatchers.IO) {
            currentPosition = realPosition
            currentOpenWord = wordListLiveData.value!!.getOrNull(realPosition)
            currentOpenWord?.let {
                if(isMarkLiveData.value != it.isMark)
                    isMarkLiveData.postValue(it.isMark)
            }

            currentOpenBook.position = realPosition
            BookRepository.updateBook(currentOpenBook)
        }
    }

    fun getWordPosition(wordId: Long) = wordListLiveData.value!!.indexOfFirst { it.id==wordId }



    fun updateMarkWord(wordId:Long, isMark: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            if(WordRepository.updateMarkWord(wordId, isMark)>0){
                databaseEvent.postValue((if(isMark) "mark" else "cancelMark") to null)
            }
        }
    }

    fun updateWord(word: Word){
        viewModelScope.launch(Dispatchers.IO) {
            if(WordRepository.updateWord(word)>0)
                databaseEvent.postValue("update" to null)
        }
    }


    fun deleteWordToTrash(wordId: Long){
        viewModelScope.launch(Dispatchers.IO) {
            if(WordRepository.deleteWordToTrash(wordId) > 0)
                databaseEvent.postValue("delete" to null)
        }
    }

    companion object {
        fun provideFactory(book: Book): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return WordCardViewModel(book) as T
            }
        }
    }
}


/*

    companion object {
        fun provideFactory(bookId:Long, bookName:String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return WordViewModel(bookId, bookName) as T
            }
        }
    }
}*/