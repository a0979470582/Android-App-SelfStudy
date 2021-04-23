package com.bu.selfstudy.ui.word


import androidx.lifecycle.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Member
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.MemberRepository
import com.bu.selfstudy.data.repository.WordRepository
import com.bu.selfstudy.tools.SingleLiveData
import com.bu.selfstudy.tools.log
import kotlinx.coroutines.launch


class WordViewModel() : ViewModel() {

    //book可能在導覽抽屜(某一個Fragment)那裡被切換
    val bookLiveData = MutableLiveData<Book>()

    //wordList會因book被切換, 或是本身單字新增刪除而改變
    val wordListLiveData = bookLiveData.switchMap {
        WordRepository.loadWords(it.id, "%").asLiveData()
    }

    /**
       wordList的position問題, 在程式運行中, ViewPager2完成了大部份處理(例如刪除某一Page時,
       ViewPager2內部的position也會更動), 並且頻繁的更動position,也不適合與資料庫同步,
       因此只在切換題庫或離開程式時同步到資料庫, 但是Member底下的currentBookId並不會
       頻繁更動, 因此可以每次切換題庫, 就和資料庫同步
    */

    //該book初始顯示的Word, 若initialWordId不存在, 就使用wordList的第一個word
    val initialWordLiveData = MediatorLiveData<Word>().apply {
        addSource(bookLiveData){ book->
            wordListLiveData.value?.let { value = combineWord(book.initialWordId, it) }
        }
        addSource(wordListLiveData){wordList->
            bookLiveData.value?.let { value = combineWord(it.initialWordId, wordList) }
        }
    }

    //該book初始顯示wordList中的哪一個word(index)
    val initialPositionLiveData = initialWordLiveData.map {
        wordListLiveData.value!!.indexOf(it)
    }

    //是否讓界面控制器去讀取Book中的initialWordId
    var isInitial = true

    //純粹是ViewPager內部的position值, 為了處理配置改變而放在此處
    val positionLiveData = MutableLiveData<Int>(0)

    //別忘記切換題庫時, 儲存當前position值
    fun updateInitialWordId(){
        if(isInitial)
            return
        val position = positionLiveData.value!!
        val wordId= wordListLiveData.value!![position].id
        val book = bookLiveData.value!!.copy()
        book.initialWordId = wordId
        updateBook(book)
    }

    private fun updateBook(book: Book){
        viewModelScope.launch {
            BookRepository.updateBook(book)
        }
    }

    private fun combineWord(initialWordId: Long, wordList:List<Word>) =
            wordList.firstOrNull { it.id == initialWordId }?: wordList[0]

}


/*



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