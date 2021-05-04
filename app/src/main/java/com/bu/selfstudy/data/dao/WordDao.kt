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

    fun loadDistinctWords(bookId:Long, query: String) =
        loadWords(bookId, query).distinctUntilChanged()

    @Query("DELETE FROM Word WHERE id =:wordId")
    fun delete(vararg wordId:Long): Int

    @Query("UPDATE Word SET isTrash = 1 WHERE id =:wordId")
    fun deleteWordToTrash(vararg wordId:Long): Int

}