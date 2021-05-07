package com.bu.selfstudy.ui.word


import androidx.lifecycle.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.WordRepository
import kotlinx.coroutines.launch

/**
 * 更改資料庫中的wordList都會透過LiveData反應在目前的ViewPager, 例如新增,刪除,修改都會同步到資料庫,
 * 接著透過LiveData傳回並在ViewPager看到更新, 而wordFragment的生命週期進入onStop時, 會同步目前使用者所
 * 看到的單字頁(wordId)到資料庫
 */
class WordViewModel() : ViewModel() {

    val bookIdLiveData = MutableLiveData<Long>(0L)

    val wordListLiveData = bookIdLiveData.switchMap {
        WordRepository.loadWords(it, "%").asLiveData()
    }

    var currentBook: Book? = null
    var currentWord: Word? = null
    var currentPosition:Int? = null


    fun getInitialPosition():Int{
        if(wordListLiveData.value == null || currentBook == null)
            return 0

        //初始化, 題庫切換的情況
        val initialPosition = wordListLiveData.value!!.indexOfFirst {
            currentBook!!.initialWordId == it.id
        }

        if (initialPosition != -1)
            return initialPosition

        //刪除單字的情況
        currentPosition?.let {
            val maxPosition = wordListLiveData.value!!.size-1
            return if(it > maxPosition)
                maxPosition
            else
                it
        }

        return 0
    }


    //將當前頁面同步到資料庫
    fun updateInitialWordId(){
        currentWord?.let {
            val currentBook = currentBook!!.copy()
            currentBook.initialWordId = it.id
            updateBook(currentBook)
        }
    }

    private fun updateBook(book: Book){
        viewModelScope.launch {
            BookRepository.updateBook(book)
        }
    }

}


/*
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
        val book = bookLiveData.value!!.copy()
        book.currentWordId = wordId
        updateBook(book)
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