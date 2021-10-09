package com.bu.selfstudy.ui.word


import android.os.Bundle
import androidx.lifecycle.*
import androidx.recyclerview.widget.SortedList
import com.bu.selfstudy.data.model.RecentWord
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.RecentWordRepository
import com.bu.selfstudy.data.repository.WordRepository
import com.bu.selfstudy.tool.SingleLiveData
import com.bu.selfstudy.tool.log
import com.bu.selfstudy.tool.putBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

/**
* 更改資料庫中的wordList都會透過LiveData反應在目前的ViewPager, 例如新增,刪除,修改都會同步到資料庫,
* 接著透過LiveData傳回並在ViewPager看到更新, 而wordFragment的生命週期進入onStop時, 會同步目前使用者所
* 看到的單字頁(wordId)到資料庫
* */
class WordViewModel : ViewModel(){

    val databaseEvent = SingleLiveData<Pair<String, Bundle?>>()
    val SortStateEnum = WordRepository.SortStateEnum
    var firstLoad = true

    var actionType = ""

    val backStack = ArrayList<Int>()

    val bookIdLiveData = MutableLiveData<Long>()

    val bookLiveData = bookIdLiveData.switchMap {
        BookRepository.loadOneBook(it).asLiveData()
    }


    val onlyMarkLiveData = MutableLiveData<Boolean>(false)
    val sortStateLiveData=  MutableLiveData<Int>(SortStateEnum.OLDEST)
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
            wordIdList.addAll(wordList.map { it.id})
        }
    }

    val markedWordIdList = ArrayList<Long>()
    fun refreshMarkedWordIdList(wordList: List<Word>){
        viewModelScope.launch {
            markedWordIdList.clear()
            markedWordIdList.addAll(wordList.filter { it.isMark }.map { it.id })
        }
    }

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
        BookRepository.updatePosition(bookLiveData.value!!.id, realPosition)
    }


    var longPressedWordIdList = ArrayList<Long>()
    fun refreshLongPressedWord(wordIdList: List<Long>){
        viewModelScope.launch(Dispatchers.Default) {
            longPressedWordIdList.clear()
            longPressedWordIdList.addAll(wordIdList)
        }
    }


    fun addBackStack(type: Int){
        backStack.remove(type)
        backStack.add(type)
    }

    fun popBackStack(): Int?{
        backStack.removeAt(backStack.lastIndex)
        return getLastBackStack()
    }

    fun getLastBackStack() = backStack.lastOrNull()

    fun getWordPosition(wordId: Long) = wordListLiveData.value!!.indexOfFirst {
        it.id==wordId
    }

    fun getWord(wordId: Long) = wordListLiveData.value!!.firstOrNull {
        it.id==wordId
    }

    fun updateMarkWord(vararg wordId:Long, isMark: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            if(WordRepository.updateMark(*wordId, isMark = isMark)>0){
                markLiveData.postValue(isMark)
                databaseEvent.postValue((if(isMark) "mark" else "cancelMark") to null)
            }
        }
    }

    fun deleteWord(vararg wordId: Long){
        viewModelScope.launch(Dispatchers.IO) {
            WordRepository.delete(*wordId).let { count ->
                if(count > 0)
                    databaseEvent.postValue("delete" to putBundle("count", count))

            }
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
}
