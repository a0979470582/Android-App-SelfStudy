package com.bu.selfstudy

import androidx.lifecycle.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Member
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.MemberRepository
import com.bu.selfstudy.data.repository.WordRepository
import com.bu.selfstudy.tool.SingleLiveData
import com.bu.selfstudy.tool.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
activityViewModel的生命週期為整個APP, 它儲存在導覽抽屜顯示的會員資料,
取出單字所需的題庫列表, 他們佔用內存不多
 */
class ActivityViewModel : ViewModel() {
    val memberLiveData = MemberRepository.loadMember().asLiveData()
    val bookListLiveData = BookRepository.loadBooks().asLiveData()

    //當前顯示的book, 這兩個來源都會影響到此變數
    val bookLiveData = MediatorLiveData<Book>().apply {
        addSource(bookListLiveData){ bookList->
            memberLiveData.value?.let { member->
                value = combineBook(member, bookList)
            }
        }
        addSource(memberLiveData){member->
            bookListLiveData.value?.let { bookList->
                value = combineBook(member, bookList)
            }
        }
    }
    private fun combineBook(member: Member, bookList:List<Book>) =
        bookList.firstOrNull { it.id == member.initialBookId }?: bookList[0]


    var book:Book? = null
    val bookList = ArrayList<Book>()
    var position = 0
    fun refreshData(){
        viewModelScope.launch(Dispatchers.Default) {
            book = bookLiveData.value!!
            bookList.clear()
            bookList.addAll(bookListLiveData.value!!)
            position = bookList.indexOf(book)
        }
    }

    fun updateInitialBookId(bookId: Long){
        viewModelScope.launch(Dispatchers.IO) {
            val member = memberLiveData.value!!.copy()
            member.initialBookId = bookId
            MemberRepository.updateMember(member)
        }
    }


    val insertEvent = SingleLiveData<List<Long>>()
    val deleteEvent = SingleLiveData<Int>()
    val deleteToTrashEvent = SingleLiveData<Int>()

    fun insertWord(word: Word){
        viewModelScope.launch(Dispatchers.IO) {
            WordRepository.insertWord(word).let {
                if(it.isNotEmpty())
                    insertEvent.postValue(it)
            }
        }
    }


    fun deleteWord(wordId: Long){
        viewModelScope.launch(Dispatchers.IO) {
            WordRepository.deleteWord(wordId).let {
                if(it>0)
                    deleteEvent.postValue(it)
            }
        }
    }

    fun deleteWordToTrash(wordId: Long){
        viewModelScope.launch(Dispatchers.IO) {
            WordRepository.deleteWordToTrash(wordId).let {
                if(it>0)
                    deleteToTrashEvent.postValue(it)
            }
        }
    }




}