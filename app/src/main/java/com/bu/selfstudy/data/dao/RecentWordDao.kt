package com.bu.selfstudy.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bu.selfstudy.data.model.RecentWord
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentWordDao: BaseDao<RecentWord>{
    //select
    @Query("SELECT * FROM RecentWord ORDER BY timestamp DESC")
    fun loadRecentWord(): Flow<List<RecentWord>>

    //insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentWord(recentWord: RecentWord): Long

}