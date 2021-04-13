package com.bu.selfstudy.data.dao

import androidx.room.*
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface BookDao : BaseDao<Book>{
    @Query("SELECT * FROM Book WHERE memberID =:memberId")
    fun loadBooks(memberId: Long=SelfStudyApplication.memberId): Flow<List<Book>>
    fun loadDistinctBooks(memberId: Long=SelfStudyApplication.memberId)
        = loadBooks(memberId).distinctUntilChanged()

}