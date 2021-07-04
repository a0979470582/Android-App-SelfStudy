package com.bu.selfstudy.ui.recentword


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
class RecentWordViewModel(val currentOpenBook: Book) : ViewModel() {

    val databaseEvent = SingleLiveData<Pair<String, Bundle?>>()

    val wordListLiveData = LivePagedListBuilder(
            WordRepository.loadWordTuplesWithPaging(currentOpenBook.id, "%"),
            Config(100)
    ).build()


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
            currentPosition?.let {
                val currentBook = currentOpenBook.copy()
                currentBook.position = it
                BookRepository.updateBook(currentBook)
            }
        }
    }

    fun updateMarkWord(wordId: Long, isMark: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            if(WordRepository.updateMarkWord(wordId, isMark)>0){
                databaseEvent.postValue((if(isMark) "mark" else "cancelMark") to null)
            }
        }
    }

    fun deleteWordToTrash(wordIdList:List<Long>){
        viewModelScope.launch(Dispatchers.IO) {
            if(WordRepository.deleteWordToTrash(*wordIdList.toLongArray()) > 0)
                databaseEvent.postValue("delete" to null)
        }
    }

    companion object {
        fun provideFactory(book: Book): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return RecentWordViewModel(book) as T
            }
        }
    }
}


/*



    fun getInitialPosition():Int{
        if(wordListLiveData.value == null || wordListLiveData.value!!.isEmpty())
            return 0
        if(this.currentOpenBookLiveData.value == null)
            return 0
        //這是初始化頁面, 題庫切換的情況, 若是刪除單字則it=-1
        wordListLiveData.value!!.indexOfFirst { this.currentOpenBookLiveData.value!!.initialWordId == it.id }
                                .let { if(it!=-1) return it }

        //這是刪除單字的情況, 盡量維持同一position
        currentPosition?.let {
            val maxPosition = wordListLiveData.value!!.size-1
            return min(maxPosition, it)
        }

        return 0
    }
    val initialPositionLiveData = wordListLiveData.map {wordList->
        val position = wordList.indexOfFirst {word->
            word.id == bookLiveData.value!!.initialWordId
        }
        if(position==-1)
            return@map 0

        return@map position
    }


    fun updateCurrentWordId(position: Int){
        val wordId = wordListLiveData.value!![position].id
        val currentOpenBook = bookLiveData.value!!.copy()
        currentOpenBook.currentWordId = wordId
        updateBook(currentOpenBook)
    }

    val wordListLD: LiveData<List<Word>> = searchQueryLD.switchMap{
        val query = if(it.isBlank()) "%" else "%$it%"
        Repository.loadWords(bookId, query).asLiveData()
    }
    init {
        Repository.getWordFragmentState().apply {
            bookId.value = getLong("bookId", 0)
            wordId.value = getLong("wordId", 0)
            sortState.value = getString("sortState", null)
            photoPosition.value = getInt("photoPosition", 0)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Repository.setWordFragmentState(
            bookId.value!!,
            wordId.value!!,
            sortState.value!!,
            photoPosition.value!!
        )
    }
    val searchQueryLD = MutableLiveData("")


    val idList = ArrayList<Long>()

    var isLoadingLD = MutableLiveData(false)
    val databaseEventLD = MutableLiveData<DatabaseResultEvent>()


    fun refresh() {
        searchQueryLD.value = searchQueryLD.value
    }

    fun insertWords(words:List<Word>){
        setDatabaseResult(isLoadingLD, databaseEventLD, "新增"){
            Repository.insertWords(words).size
        }
    }

    fun deleteWordsToTrash(wordIds: List<Long>){
        setDatabaseResult(isLoadingLD, databaseEventLD, "刪除"){
            Repository.updateWords(
                getWordList(wordIds).onEach { it.isTrash = true })
        }
    }

    fun copyWordsTo(wordIds: List<Long>, bookId: Long){
        setDatabaseResult(isLoadingLD, databaseEventLD, "複製"){
            Repository.insertWords(getWordList(wordIds).onEach {
                it.id = 0
                it.bookId = bookId
            }).size
        }
    }

    fun moveWordsTo(wordIds: List<Long>, bookId: Long){
        setDatabaseResult(isLoadingLD, databaseEventLD, "轉移"){
            Repository.updateWords(
                getWordList(wordIds).onEach { it.bookId = bookId }
            )
        }
    }

    fun setIdList(words: List<Word>) {
        idList.clear()
        idList.addAll(words.map { it.id })
    }

    private fun getWordList(wordIds: List<Long>) =
            wordListLD.value!!.filter { wordIds.contains(it.id) }

    companion object {
        fun provideFactory(bookId:Long, bookName:String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return WordViewModel(bookId, bookName) as T
            }
        }
    }
}*/