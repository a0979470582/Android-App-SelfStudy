package com.bu.selfstudy.logic.dao

import androidx.room.*
import com.bu.selfstudy.logic.model.Member

@Dao
interface MemberDao{
    @Query("SELECT * FROM Member WHERE id=:memberId")
    fun loadMemberById(memberId: Int): Member

    @Update
    fun updateMember(member: Member):Int //return success's count

    @Insert
    fun insertMember(member: Member):Long //return rowId

    @Delete
    fun deleteMember(member: Member):Int //return success's count
}
