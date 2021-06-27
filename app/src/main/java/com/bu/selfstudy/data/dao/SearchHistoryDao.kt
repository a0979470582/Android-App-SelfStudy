package com.bu.selfstudy.data.dao

import androidx.room.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Member
import com.bu.selfstudy.data.model.SearchHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface SearchHistoryDao: BaseDao<SearchHistory>{
    //new search(time more big then old) sort to top
    @Query("SELECT * FROM SearchHistory WHERE searchName LIKE :query || '%' ORDER BY timestamp DESC")
    fun loadSearchHistory(query: String): Flow<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)//new search replace old
    suspend fun insertSearchHistory(searchHistory: SearchHistory): Long
}
