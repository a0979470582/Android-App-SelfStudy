package com.bu.selfstudy.ui.word


import androidx.lifecycle.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Member
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.MemberRepository
import com.bu.selfstudy.data.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class WordViewModel() : ViewModel() {


    val currentBookIdLiveData = MutableLiveData<Long>()

    val wordListLiveData = currentBookIdLiveData.switchMap {
        WordRepository.loadWords(it, "%").asLiveData()
    }




}


/*





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