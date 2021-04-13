package com.bu.selfstudy.data.dao

import androidx.room.*
import com.bu.selfstudy.data.model.Member
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface BaseDao<T> {
    @Update
    suspend fun update(vararg obj: T):Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg obj: T):List<Long>

    @Delete
    suspend fun delete(vararg obj: T):Int
}