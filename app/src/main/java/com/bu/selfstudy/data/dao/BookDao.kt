package com.bu.selfstudy.data.dao

import androidx.room.*
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface BookDao : BaseDao<Book>{
    @Query("SELECT * FROM Book WHERE isTrash=0 AND memberID =:memberId")
    fun loadBooks(memberId: Long=SelfStudyApplication.memberId): Flow<List<Book>>
    fun loadDistinctBooks(memberId: Long=SelfStudyApplication.memberId)
        = loadBooks(memberId).distinctUntilChanged()

    @Query("DELETE FROM Book WHERE id=:bookId")
    suspend fun delete(bookId: Long): Int
    @Query("UPDATE Book SET isTrash = 1 WHERE id=:bookId")
    suspend fun deleteBookToTrash(bookId: Long): Int

    @Query("UPDATE Book SET size=(SELECT COUNT(id) FROM Word WHERE bookId=:bookId) WHERE id=:bookId")
    suspend fun updateBookSize(bookId: Long): Int
}