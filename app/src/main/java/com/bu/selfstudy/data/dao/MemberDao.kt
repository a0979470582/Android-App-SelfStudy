package com.bu.selfstudy.data.dao

import androidx.room.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Member
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface MemberDao: BaseDao<Member>{
    //select
    @Query("SELECT * FROM Member WHERE id=:memberId")
    fun loadMember(memberId: Long): Flow<Member>

    fun loadDistinctMember(memberId: Long) =
            loadMember(memberId).distinctUntilChanged()

}
