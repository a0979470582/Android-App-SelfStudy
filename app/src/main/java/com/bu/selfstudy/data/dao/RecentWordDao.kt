package com.bu.selfstudy.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bu.selfstudy.data.model.RecentWord
import androidx.paging.DataSource

@Dao
interface RecentWordDao {
    //select
    @Query("SELECT * FROM RecentWord ORDER BY timestamp DESC")
    fun loadRecentWord(): DataSource.Factory<Int, RecentWord>

    //insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentWord(recentWord: RecentWord): Long



}