package com.bu.selfstudy.data.dao

import androidx.room.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Member
import com.bu.selfstudy.data.model.SearchHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface SearchHistoryDao: BaseDao<SearchHistory>{
    //select
    @Query("""SELECT * FROM SearchHistory
                    WHERE searchName>=:query AND searchName<=:query||'z'
                    ORDER BY timestamp DESC""")
    fun loadHistory(query: String): Flow<List<SearchHistory>>

    @Query("""SELECT count(*)>0 FROM SearchHistory
                    WHERE searchName = :searchName""")
    fun checkHistoryExists(searchName: String): Boolean

    //insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)//new search replace old
    suspend fun insertHistory(history: SearchHistory): Long

    //delete
    @Query("DELETE FROM SearchHistory")
    suspend fun deleteAll(): Int
}
