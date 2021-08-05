package com.bu.selfstudy.ui.wordlist


import android.os.Bundle
import androidx.lifecycle.*
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.WordRepository
import com.bu.selfstudy.tool.SingleLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 上一個fragment傳入bookId, 另外設置有默認值的兩個條件onlyMark和sortState,
 * 這三個變數的變更會觸發wordlist刷新
 */
class WordListViewModel() : ViewModel() {

    val databaseEvent = SingleLiveData<Pair<String, Bundle?>>()

    val SortStateEnum = WordRepository.SortStateEnum

    val bookIdLiveData = MutableLiveData<Long>()
    val onlyMarkLiveData = MutableLiveData<Boolean>(false)
    val sortStateLiveData=  MutableLiveData<Int>(SortStateEnum.OLDEST)


    val bookLiveData = bookIdLiveData.switchMap {
        BookRepository.loadOneBook(it).asLiveData()
    }

    val conditionLiveData = MediatorLiveData<Boolean>().also {
        it.addSource(bookIdLiveData){
            combineCondition()
        }
        it.addSource(onlyMarkLiveData){
            combineCondition()
        }
        it.addSource(sortStateLiveData){
            combineCondition()
        }
    }

    private fun combineCondition(){
        if(bookIdLiveData.value == null)
            return

        conditionLiveData.value = true
    }

    val wordListLiveData = conditionLiveData.switchMap {
        WordRepository.loadBookWords(
                bookId = bookIdLiveData.value!!,
                sortState = sortStateLiveData.value!!,
                onlyMark = onlyMarkLiveData.value!!
        ).asLiveData()
    }





    val wordIdList = ArrayList<Long>()
    fun refreshWordIdList(wordList: List<Word>){
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
            if(WordRepository.updateMark(wordId, isMark)>0){
                databaseEvent.postValue((if(isMark) "mark" else "cancelMark") to null)
            }
        }
    }

    fun deleteWord(wordIdList:List<Long>){
        viewModelScope.launch(Dispatchers.IO) {
            if(WordRepository.delete(*wordIdList.toLongArray()) > 0)
                databaseEvent.postValue("delete" to null)
        }
    }



}