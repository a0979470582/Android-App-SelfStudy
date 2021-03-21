package com.bu.selfstudy.logic.dao

import androidx.room.*
import com.bu.selfstudy.logic.model.Member
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao{
    @Query("SELECT * FROM Member WHERE id=:memberId")
    fun loadMemberById(memberId: Int): Flow<Member>

    @Update
    suspend fun updateMember(member: Member):Int //return success's count

    @Insert
    suspend fun insertMember(member: Member):Long //return rowId

    @Delete
    suspend fun deleteMember(member: Member):Int //return success's count
}
