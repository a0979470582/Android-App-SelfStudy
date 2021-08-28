package com.bu.selfstudy.ui.main

import android.os.Bundle
import androidx.lifecycle.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.MemberRepository
import com.bu.selfstudy.data.repository.WordRepository
import com.bu.selfstudy.tool.SingleLiveData
import com.bu.selfstudy.tool.putBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
activityViewModel的生命週期為整個APP, 它儲存在導覽抽屜顯示的會員資料,
取出單字所需的題庫列表, 他們佔用內存不多
 */
class ActivityViewModel : ViewModel() {
    val databaseEvent = SingleLiveData<Pair<String, Bundle?>>()

    val memberLiveData = MemberRepository.loadMember().asLiveData()
    val bookListLiveData = BookRepository.loadBooks().asLiveData()


    val bookIdList = ArrayList<Long>()
    fun refreshBookIdList(bookList: List<Book>){
        viewModelScope.launch(Dispatchers.IO) {
            bookIdList.clear()
            bookIdList.addAll(bookList.map { it.id })
        }
    }


    //新增單字是多個頁面都可進行的操作
    fun insertWord(word: Word){
        viewModelScope.launch(Dispatchers.IO) {
            val resultIdList = WordRepository.insertWord(word)
            if(resultIdList.isNotEmpty())
                databaseEvent.postValue("insertWord" to
                        putBundle("wordId", resultIdList.first())
                        .putBundle("bookId", word.bookId))
        }
    }

    fun insertBook(book: Book){
        viewModelScope.launch {
            if(BookRepository.insertBook(book).isNotEmpty()) {
                databaseEvent.postValue("insertBook" to null)
            }
        }
    }

    fun getBookName(bookId: Long) = bookListLiveData.value!!.firstOrNull { it.id==bookId }?.bookName?:""
}