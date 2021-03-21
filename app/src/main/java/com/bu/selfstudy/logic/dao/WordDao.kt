package com.bu.selfstudy.logic.dao

import androidx.room.*
import com.bu.selfstudy.logic.model.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM Word WHERE isTrash=0 AND bookId=:bookId AND name LIKE :query")
    fun loadWords(bookId:Long, query: String): Flow<List<Word>>

    @Update
    suspend fun updateWords(vararg words: Word):Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(vararg words: Word):List<Long>

    @Delete
    suspend fun deleteWords(vararg words: Word):Int
}