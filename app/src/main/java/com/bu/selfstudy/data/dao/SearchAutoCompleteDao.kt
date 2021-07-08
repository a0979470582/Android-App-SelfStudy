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
    //select
    @Query("""SELECT * FROM SearchAutoComplete
                 WHERE searchName LIKE :query || '%' AND isHistory=0
                ORDER BY searchName ASC""")
    fun loadAutoComplete(query: String): Flow<List<SearchAutoComplete>>

    //insert
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAutoComplete(vararg autoComplete: SearchAutoComplete): List<Long>

    //update
    @Query("UPDATE SearchAutoComplete SET isHistory=:isHistory WHERE searchName IN (:searchName)")
    suspend fun setIsHistory(vararg searchName: String, isHistory: Boolean): Int
}
