package com.bu.selfstudy.logic.dao

import androidx.room.*
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.logic.model.Book
import com.bu.selfstudy.logic.model.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM Book WHERE id IN (:bookIds)")
    fun loadBooksByIds(bookIds: IntArray): Flow<List<Book>>

    @Query("SELECT * FROM Book WHERE memberID =:memberId")
    fun loadBooksByMemberId(memberId: Int=SelfStudyApplication.memberId): Flow<List<Book>>

    @Update
    suspend fun updateBook(book: Book):Int

    @Insert
    suspend fun insertBook(book: Book):Long

    @Insert
    suspend fun insertBooks(vararg books: Book):List<Long>

    @Delete
    suspend fun deleteBook(book: Book):Int
}