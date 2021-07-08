package com.bu.selfstudy.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bu.selfstudy.data.model.RecentWord
import com.bu.selfstudy.data.model.SearchAutoComplete
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentWordDao {
    //select
    @Query("SELECT * FROM  WHERE searchName LIKE :query || '%' AND isHistory=0 ORDER BY searchName ASC")
    fun loadRecentWord(query: String): Flow<List<RecentWord>>

    //insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentWord(recentWord: RecentWord): Long

}