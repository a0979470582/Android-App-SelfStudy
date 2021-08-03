package com.bu.selfstudy.ui.wordlist


import android.os.Bundle
import androidx.lifecycle.*
import androidx.paging.Config
import androidx.paging.LivePagedListBuilder
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.model.WordTuple
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.WordRepository
import com.bu.selfstudy.tool.SingleLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 更改資料庫中的wordList都會透過LiveData反應在目前的ViewPager, 例如新增,刪除,修改都會同步到資料庫,
 * 接著透過LiveData傳回並在ViewPager看到更新, 而wordFragment的生命週期進入onStop時, 會同步目前使用者所
 * 看到的單字頁(wordId)到資料庫
 */
class WordListViewModel() : ViewModel() {

    val databaseEvent = SingleLiveData<Pair<String, Bundle?>>()



    val bookIdLiveData = MutableLiveData<Long>()

    val bookLiveData = bookIdLiveData.switchMap {
        BookRepository.loadBook(it).asLiveData()
    }

    val wordListLiveData = bookIdLiveData.switchMap {
        LivePagedListBuilder(
                WordRepository.loadWordTuplesWithPaging(it, "%"),
                Config(100)
        ).build()
    }


    val wordIdList = ArrayList<Long>()
    fun refreshWordIdList(wordList: List<WordTuple>){
        viewModelScope.launch {
            wordIdList.clear()
            wordIdList.addAll(wordList.map { if(it != null) it.id else 0 })
        }
    }

    var longPressedWordIdList = ArrayList<Long>()
    fun refreshLongPressedWord(wordIdList: List<Long>){
        viewModelScope.launch {
            longPressedWordIdList.clear()
            longPressedWordIdList.addAll(wordIdList)
        }
    }


    var currentOpenWord: Word? = null
    var currentPosition: Int? = null

    //將當前頁面同步到資料庫
    fun updateCurrentPosition(){
        viewModelScope.launch {
            if(currentPosition==null || bookLiveData.value==null)
                return@launch

            BookRepository.updateBook(
                bookLiveData.value!!.copy().also {
                    it.position = currentPosition!!
                }
            )

        }
    }

    fun updateMarkWord(wordId: Long, isMark: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            if(WordRepository.updateWordMark(wordId, isMark)>0){
                databaseEvent.postValue((if(isMark) "mark" else "cancelMark") to null)
            }
        }
    }

    fun deleteWordToTrash(wordIdList:List<Long>){
        viewModelScope.launch(Dispatchers.IO) {
            if(WordRepository.updateWordIsTrash(*wordIdList.toLongArray(),
                            isTrash = true) > 0
            )
                databaseEvent.postValue("delete" to null)
        }
    }

}