package com.bu.selfstudy.ui.mark


import android.os.Bundle
import androidx.lifecycle.*
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.WordRepository
import com.bu.selfstudy.tool.SingleLiveData
import com.bu.selfstudy.tool.putBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 上一個fragment傳入bookId, 另外設置有默認值的兩個條件onlyMark和sortState,
 * 這三個變數的變更會觸發wordlist刷新
 */
class MarkViewModel() : ViewModel() {

    val databaseEvent = SingleLiveData<Pair<String, Bundle?>>()

    val wordListLiveData = WordRepository.loadMarkWords().asLiveData()

    var actionType = ""

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

    fun updateMarkWord(vararg wordId: Long, isMark: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            if(WordRepository.updateMark(*wordId, isMark = isMark)>0){
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

    fun copyWord(bookId: Long){
        viewModelScope.launch {
            val count = longPressedWordIdList.size
            if(WordRepository.copyWord(longPressedWordIdList, bookId)>0)
                databaseEvent.postValue("copy" to (
                        putBundle("bookId", bookId)
                                .putBundle("count", count)
                        )
                )
        }
    }
    fun moveWord(bookId: Long){
        viewModelScope.launch {
            val count = longPressedWordIdList.size
            if(WordRepository.moveWord(longPressedWordIdList, bookId)>0)
                databaseEvent.postValue("move" to (
                        putBundle("bookId", bookId)
                                .putBundle("count", count)
                        )
                )
        }
    }
    fun getWord(wordId: Long) = wordListLiveData.value!!.firstOrNull {
        it.id==wordId
    }
}