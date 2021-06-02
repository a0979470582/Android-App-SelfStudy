package com.bu.selfstudy

import androidx.lifecycle.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.MemberRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
activityViewModel的生命週期為整個APP, 它儲存在導覽抽屜顯示的會員資料,
取出單字所需的題庫列表, 他們佔用內存不多
 */
class ActivityViewModel : ViewModel() {
    val memberLiveData = MemberRepository.loadMember().asLiveData()
    val bookListLiveData = BookRepository.loadBooks().asLiveData()

    val bookIdList = ArrayList<Long>()
    fun refreshBookIdList(bookList: List<Book>){
        viewModelScope.launch {
            bookIdList.clear()
            bookIdList.addAll(bookList.map { it.id })
        }
    }

    val currentOpenBookLiveData = MutableLiveData<Book>()
    fun refreshCurrentOpenBook(bookId: Long){
        viewModelScope.launch {
            bookListLiveData.value?.let {bookList->
                currentOpenBookLiveData.value = bookList.firstOrNull { it.id==bookId }
            }
        }
    }

}

/*
    //當前顯示的book, 這兩個來源都會影響到此變數
    val bookLiveData = MediatorLiveData<Book>().apply {
        addSource(bookListLiveData){ bookList->
            memberLiveData.value?.let { member->
                value = combineBook(member, bookList)
            }
        }
        addSource(memberLiveData){member->
            bookListLiveData.value?.let { bookList->
                if(bookList.isNotEmpty())
                    value = combineBook(member, bookList)
            }
        }
    }
    private fun combineBook(member: Member, bookList:List<Book>) =
        bookList.firstOrNull { it.id == member.initialBookId }?: bookList[0]
* */