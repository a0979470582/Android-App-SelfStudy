package com.bu.selfstudy.data.repository

import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Member
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MemberRepository {
    val memberDao = getDatabase().memberDao()

    fun loadMember(memberId:Long=SelfStudyApplication.memberId)
        = memberDao.loadDistinctMember(memberId)


    suspend fun insertMember(member: Member) = withContext(Dispatchers.IO){
        memberDao.insert(member)
    }
    suspend fun updateMember(member: Member) = withContext(Dispatchers.IO){
        memberDao.update(member)
    }
    suspend fun deleteMember(member: Member) = withContext(Dispatchers.IO){
        memberDao.delete(member)
    }


}