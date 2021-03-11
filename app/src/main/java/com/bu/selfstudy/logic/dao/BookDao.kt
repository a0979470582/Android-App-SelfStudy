package com.bu.selfstudy.logic.dao

import androidx.room.*
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.logic.model.Book
import com.bu.selfstudy.logic.model.Word

@Dao
interface BookDao {

    @Query("SELECT * FROM Book WHERE id IN (:bookIds)")
    fun loadBooksByIds(bookIds: IntArray): List<Book>

    @Query("SELECT * FROM Book WHERE memberID =:memberId")
    fun loadBooksByMemberId(memberId: Int=SelfStudyApplication.memberId): List<Book>

    @Update
    fun updateBook(book: Book):Int

    @Insert
    fun insertBook(book: Book):Long

    @Insert
    fun insertBooks(vararg books: Book):List<Long>

    @Delete
    fun deleteBook(book: Book):Int
}