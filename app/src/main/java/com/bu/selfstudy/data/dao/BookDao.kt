package com.bu.selfstudy.data.dao

import android.content.res.ColorStateList
import androidx.room.*
import com.bu.selfstudy.R
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.tool.log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface BookDao : BaseDao<Book>{
    //select
    @Query("SELECT * FROM Book WHERE isTrash=0 AND memberID =:memberId")
    fun loadBooks(memberId: Long=SelfStudyApplication.memberId): Flow<List<Book>>

    fun loadDistinctBooks(memberId: Long=SelfStudyApplication.memberId) =
            loadBooks(memberId).distinctUntilChanged()

    //update
    @Query("UPDATE Book SET isTrash = :isTrash WHERE id=:bookId")
    suspend fun updateBookIsTrash(bookId: Long, isTrash: Boolean): Int

    @Query("UPDATE Book SET size=(SELECT COUNT(Word.id) FROM Word WHERE bookId=Book.id AND Word.isTrash=0)")
    suspend fun updateBookSize(): Int

    @Query("UPDATE Book SET position=:position WHERE id=:bookId")
    suspend fun updateBookPosition(bookId: Long, position: Int): Int

    //delete
    @Query("DELETE FROM Book WHERE id=:bookId")
    suspend fun delete(bookId: Long): Int

}