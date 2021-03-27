package com.bu.selfstudy.logic.dao

import androidx.room.*
import com.bu.selfstudy.logic.model.Member
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao{
    @Query("SELECT * FROM Member WHERE id=:memberId")
    fun loadMember(memberId: Long): Flow<Member>

    @Update
    suspend fun updateMember(member: Member):Int //return success's count

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMember(member: Member):Long //return rowId

    @Delete
    suspend fun deleteMember(member: Member):Int //return success's count
}
