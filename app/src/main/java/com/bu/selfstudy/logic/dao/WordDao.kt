package com.bu.selfstudy.logic.dao

import androidx.room.*
import com.bu.selfstudy.logic.model.Word

@Dao
interface WordDao {
    @Query("SELECT * FROM Word WHERE bookId=:bookId")
    fun loadWordsByBookId(bookId: Long): List<Word>

    @Query("SELECT * FROM Word WHERE name LIKE :name")
    fun findWordsByName(name:String): List<Word>

    @Query("SELECT * FROM Word WHERE bookId=:bookId AND name LIKE :name")
    fun findWordsByNameAndBookId(name:String, bookId:Long): List<Word>

    @Update
    fun updateWords(vararg words: Word):Int

    @Insert
    fun insertWords(vararg words: Word):List<Long>

    @Delete
    fun deleteWords(vararg words: Word):Int
}