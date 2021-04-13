package com.bu.selfstudy.data.dao

import androidx.room.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface WordDao : BaseDao<Word>{
    @Query("SELECT * FROM Word WHERE isTrash=0 AND bookId=:bookId AND wordName LIKE :query")
    fun loadWords(bookId:Long, query: String): Flow<List<Word>>

    fun loadWordsDistinctUntilChanged(bookId:Long, query: String) =
        loadWords(bookId, query).distinctUntilChanged()

}