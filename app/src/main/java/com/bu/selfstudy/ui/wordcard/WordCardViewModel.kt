package com.bu.selfstudy.ui.wordcard


import androidx.lifecycle.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.WordRepository
import com.bu.selfstudy.tool.SingleLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * 更改資料庫中的wordList都會透過LiveData反應在目前的ViewPager, 例如新增,刪除,修改都會同步到資料庫,
 * 接著透過LiveData傳回並在ViewPager看到更新, 而wordFragment的生命週期進入onStop時, 會同步目前使用者所
 * 看到的單字頁(wordId)到資料庫
 */
class WordCardViewModel() : ViewModel() {
    val currentOpenBookLiveData = MutableLiveData<Book>()

    val wordListLiveData = currentOpenBookLiveData.switchMap {
        WordRepository.loadWords(it.id, "%").asLiveData()
    }

    var currentOpenWord: Word? = null
    var currentPosition: Int? = null

    //將當前頁面同步到資料庫
    fun updateCurrentPosition(realPosition: Int){
        viewModelScope.launch(Dispatchers.IO) {
            currentPosition = realPosition
            currentOpenWord = wordListLiveData.value!!.getOrNull(realPosition)

            with(currentOpenBookLiveData.value!!.copy()){
                this.position = realPosition
                updateBook(this)
            }
        }
    }

    private fun updateBook(book: Book){
        viewModelScope.launch {
            BookRepository.updateBook(book)
        }
    }

    fun updateMarkWord(wordId:Long, isMark: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            WordRepository.updateMarkWord(wordId, isMark).let {
                if(it>0) {
                    when(isMark){
                        true->markEvent.postValue("mark")
                        false->markEvent.postValue("cancel_mark")
                    }

                }
            }
        }
    }


    val insertEvent = SingleLiveData<List<Long>>()
    val deleteEvent = SingleLiveData<Int>()
    val deleteToTrashEvent = SingleLiveData<Int>()
    val updateEvent = SingleLiveData<Int>()
    val markEvent = SingleLiveData<String>()

    fun markWord(word: Word){
        viewModelScope.launch(Dispatchers.IO) {
            WordRepository.updateWord(word, bookId = currentOpenBookLiveData.value!!.id).let {
                if(it>0) {
                    when(word.isMark){
                        true->markEvent.postValue("mark")
                        false->markEvent.postValue("cancel_mark")
                    }

                }
            }
        }
    }

    fun updateWord(word: Word){
        viewModelScope.launch(Dispatchers.IO) {
            WordRepository.updateWord(word,bookId = currentOpenBookLiveData.value!!.id).let {
                if(it>0)
                    updateEvent.postValue(it)
            }
        }
    }



    fun insertWord(word: Word){
        viewModelScope.launch(Dispatchers.IO) {
            WordRepository.insertWord(word,bookId = currentOpenBookLiveData.value!!.id).let {
                if(it.isNotEmpty())
                    insertEvent.postValue(it)
            }
        }
    }



    fun deleteWord(wordId: Long){
        viewModelScope.launch(Dispatchers.IO) {
            WordRepository.deleteWord(wordId,bookId = currentOpenBookLiveData.value!!.id).let {
                if(it>0)
                    deleteEvent.postValue(it)
            }
        }
    }

    fun deleteWordToTrash(wordId: Long){
        viewModelScope.launch(Dispatchers.IO) {
            WordRepository.deleteWordToTrash(wordId, bookId = currentOpenBookLiveData.value!!.id).let {
                if(it>0)
                    deleteToTrashEvent.postValue(it)
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