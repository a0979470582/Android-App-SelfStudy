package com.bu.selfstudy.ui

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Member
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.MemberRepository
import kotlinx.coroutines.launch


class ActivityViewModel : ViewModel() {
    val memberLiveData = MemberRepository.loadMember().asLiveData()
    val bookListLiveData = BookRepository.loadBooks().asLiveData()
    var bookList = ArrayList<Book>()

    val currentBookIdLiveData = MediatorLiveData<Long>()


    init {
        currentBookIdLiveData.addSource(bookListLiveData){bookList->
            memberLiveData.value?.let{member->
                currentBookIdLiveData.value = combineBookId(member, bookList)
            }
        }
        currentBookIdLiveData.addSource(memberLiveData){member->
            bookListLiveData.value?.let{bookList->
                currentBookIdLiveData.value = combineBookId(member, bookList)
            }
        }
    }

    fun updateMember(member: Member){
        viewModelScope.launch {
            MemberRepository.updateMember(member)
        }
    }

    private fun combineBookId(member: Member, bookList:List<Book>) =
        if(bookList.any { it.id == member.currentBookId })
            member.currentBookId
        else
            bookList[0].id
}