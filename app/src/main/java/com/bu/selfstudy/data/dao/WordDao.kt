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

    @Query("SELECT * FROM Word WHERE isTrash=0 AND id=:wordId")
    fun loadWord(wordId:Long): Flow<Word>

    fun loadDistinctWord(wordId:Long) =
        loadWord(wordId).distinctUntilChanged()

    @Query("DELETE FROM Word WHERE id =:wordId")
    suspend fun delete(vararg wordId:Long): Int

    @Query("UPDATE Word SET isTrash = 1 WHERE id =:wordId")
    suspend fun deleteWordToTrash(vararg wordId:Long): Int

    @Query("UPDATE Word SET isTrash = 1 WHERE bookid =:bookId")
    suspend fun deleteOneBookToTrash(bookId: Long): Int



    @Update
    override suspend fun update(vararg word:Word): Int
}