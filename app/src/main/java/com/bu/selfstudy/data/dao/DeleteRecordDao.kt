package com.bu.selfstudy.data.dao

import androidx.room.*
import com.bu.selfstudy.data.model.DeleteRecord
import com.bu.selfstudy.data.model.DeleteRecordBook
import com.bu.selfstudy.data.model.DeleteRecordWord
import com.bu.selfstudy.data.model.RecentWord
import kotlinx.coroutines.flow.Flow

@Dao
interface DeleteRecordDao{
    //select
    @Query("SELECT * FROM DeleteRecordBook ORDER BY timestamp DESC")
    fun loadDeleteRecordBook(): Flow<List<DeleteRecordBook>>

    @Query("SELECT * FROM DeleteRecordWord ORDER BY timestamp DESC")
    fun loadDeleteRecordWord(): Flow<List<DeleteRecordWord>>

    //insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeleteRecordBook(vararg record: DeleteRecordBook): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeleteRecordWord(vararg record: DeleteRecordWord): List<Long>


    //delete
    @Query("DELETE FROM DeleteRecordBook WHERE bookId=:bookId")
    suspend fun removeDeleteRecordBook(bookId: Long): Int

    //delete
    @Query("DELETE FROM DeleteRecordWord WHERE wordId=:wordId")
    suspend fun removeDeleteRecordWord(vararg wordId: Long): Int

    //update
    @Update
    suspend fun updateDeleteRecordWord(vararg deleteRecordWord: DeleteRecordWord): Int
}