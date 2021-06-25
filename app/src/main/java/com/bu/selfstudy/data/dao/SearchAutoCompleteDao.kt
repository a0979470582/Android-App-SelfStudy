package com.bu.selfstudy.data.dao

import androidx.room.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Member
import com.bu.selfstudy.data.model.SearchAutoComplete
import com.bu.selfstudy.data.model.SearchHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface SearchAutoCompleteDao: BaseDao<SearchAutoComplete>{
    @Query("SELECT * FROM SearchAutoComplete WHERE searchName LIKE :query || '%'")
    fun loadSearchAutoComplete(query: String): Flow<List<SearchAutoComplete>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSearchAutoComplete(vararg searchHistory: SearchHistory): Long
}
