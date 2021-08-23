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
    @Query("SELECT * FROM Book WHERE id =:bookId")
    fun loadOneBook(bookId: Long): Flow<Book>

    @Query("SELECT * FROM Book WHERE isArchive=1  AND memberID =:memberId Order by timestamp DESC")
    fun loadBooksArchived(memberId: Long=SelfStudyApplication.memberId): Flow<List<Book>>

    @Query("SELECT * FROM Book WHERE isArchive=0  AND memberID =:memberId Order by timestamp DESC")
    fun loadBooks(memberId: Long=SelfStudyApplication.memberId): Flow<List<Book>>

    //update
    @Query("UPDATE Book SET isArchive = :isArchive WHERE id=:bookId")
    suspend fun updateIsArchive(bookId: Long, isArchive: Boolean): Int

    @Query("UPDATE Book SET size=(SELECT COUNT(Word.id) FROM Word WHERE bookId=:bookId) WHERE Book.id=:bookId")
    suspend fun updateSize(bookId: Long): Int

    @Query("UPDATE Book SET size=(SELECT COUNT(Word.id) FROM Word WHERE bookId=Book.id)")
    suspend fun updateSizeAllBook(): Int

    @Query("UPDATE Book SET position=:position WHERE id=:bookId")
    suspend fun updatePosition(bookId: Long, position: Int): Int

    @Query("UPDATE Book SET colorInt=:colorInt WHERE id=:bookId")
    suspend fun updateColorInt(bookId: Long, colorInt: Int): Int

    //delete
    suspend fun delete(bookId: Long) = deleteBook(bookId).also {
        deleteBookWord(bookId)
    }

    @Query("DELETE FROM Book WHERE id=:bookId")
    suspend fun deleteBook(bookId: Long): Int

    @Query("DELETE FROM Word WHERE bookid=:bookId")
    suspend fun deleteBookWord(bookId: Long): Int
}