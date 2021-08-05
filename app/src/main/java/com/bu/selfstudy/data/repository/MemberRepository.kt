package com.bu.selfstudy.data.repository

import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Member
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MemberRepository {
    private val memberDao = getDatabase().memberDao()

    fun loadMember(memberId:Long=SelfStudyApplication.memberId)
        = memberDao.loadDistinctMember(memberId)

}