package com.bu.selfstudy.ui

import androidx.lifecycle.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Member
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.MemberRepository
import kotlinx.coroutines.launch


class ActivityViewModel : ViewModel() {
    val memberLiveData = MemberRepository.loadMember().asLiveData()
    val bookListLiveData = BookRepository.loadBooks().asLiveData()

    val bookLiveData = MediatorLiveData<Book>().apply {
        addSource(bookListLiveData){bookList->
            memberLiveData.value?.let { value = combineBook(it, bookList) }
        }
        addSource(memberLiveData){member->
            bookListLiveData.value?.let { value = combineBook(member, it) }
        }
    }


    fun updateMember(member: Member){
        viewModelScope.launch {
            MemberRepository.updateMember(member)
        }
    }

    private fun combineBook(member: Member, bookList:List<Book>) =
        bookList.firstOrNull { it.id == member.initialBookId }?: bookList[0]

}