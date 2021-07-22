package com.bu.selfstudy.ui.wordcard


import android.os.Bundle
import androidx.lifecycle.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.RecentWord
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.RecentWordRepository
import com.bu.selfstudy.data.repository.WordRepository
import com.bu.selfstudy.tool.SingleLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

  /**
 * 更改資料庫中的wordList都會透過LiveData反應在目前的ViewPager, 例如新增,刪除,修改都會同步到資料庫,
 * 接著透過LiveData傳回並在ViewPager看到更新, 而wordFragment的生命週期進入onStop時, 會同步目前使用者所
 * 看到的單字頁(wordId)到資料庫
 */
class WordCardViewModel : ViewModel() {

    val bookLiveData = MutableLiveData<Book>()

    val wordListLiveData = bookLiveData.switchMap {
        WordRepository.loadWords(it.id, "%").asLiveData()
    }

    val databaseEvent = SingleLiveData<Pair<String, Bundle?>>()

    var firstLoad = true



    //將當前頁面同步到資料庫
    var currentOpenWord: Word? = null
    var currentPosition: Int? = null
    var markLiveData = MutableLiveData<Boolean>(false)
    fun updateCurrentPosition(realPosition: Int) = viewModelScope.launch(Dispatchers.IO) {
        launch {
            currentPosition = realPosition
            currentOpenWord = wordListLiveData.value!!.getOrNull(realPosition)
            currentOpenWord?.let {
                markLiveData.postValue(it.isMark)
                insertRecentWord(wordId = it.id)
            }
        }

        bookLiveData.value!!.position = realPosition
        BookRepository.updateBookPosition(bookLiveData.value!!.id, realPosition)
    }


    fun getWordPosition(wordId: Long) = wordListLiveData.value!!.indexOfFirst {
        it.id==wordId
    }


    fun updateMarkWord(wordId:Long, isMark: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            if(WordRepository.updateWordMark(wordId, isMark)>0){
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
            if(WordRepository.updateWordIsTrash(wordId, isTrash = true) > 0)
                databaseEvent.postValue("delete" to null)
        }
    }

    private fun insertRecentWord(wordId: Long) {
        viewModelScope.launch{
            RecentWordRepository.insertRecentWord(
                RecentWord(wordId = wordId)
            )
        }
    }

    fun downloadAudio(wordId: Long) {
        viewModelScope.launch {
            WordRepository.downloadAudio(wordId)
        }
    }

  }
/*
    companion object {
        fun provideFactory(book: Book): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return WordCardViewModel(book) as T
            }
        }
    }
}*/