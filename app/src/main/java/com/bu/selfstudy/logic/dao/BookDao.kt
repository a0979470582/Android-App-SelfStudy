package com.bu.selfstudy.logic.dao

import androidx.room.*
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.logic.model.Book
import com.bu.selfstudy.logic.model.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM Book WHERE memberID =:memberId")
    fun loadBooks(memberId: Long=SelfStudyApplication.memberId): Flow<List<Book>>

    @Update
    suspend fun updateBook(book: Book):Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBook(book: Book):Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBooks(vararg book:Book):List<Long>

    @Delete
    suspend fun deleteBook(book: Book):Int
}