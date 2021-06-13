package com.bu.selfstudy.data.dao

import androidx.paging.DataSource
import androidx.room.*
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.model.WordTuple
import com.bu.selfstudy.data.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

@Dao
interface WordDao : BaseDao<Word>{
    //select
    @Query("SELECT * FROM Word WHERE isTrash=0 AND id=:wordId")
    fun loadWord(wordId:Long): Flow<Word>
    fun loadDistinctWord(wordId:Long) = loadWord(wordId).distinctUntilChanged()

    @Query("SELECT * FROM Word WHERE isTrash=0 AND bookId=:bookId AND wordName LIKE :query")
    fun loadWords(bookId:Long, query: String): Flow<List<Word>>
    fun loadDistinctWords(bookId:Long, query: String) = loadWords(bookId, query).distinctUntilChanged()

    @Query("SELECT * FROM Word WHERE isTrash=0 AND bookId=:bookId AND wordName LIKE :query")
    fun loadWordsWithPaging(bookId:Long, query: String): DataSource.Factory<Int, Word>

    @Query("SELECT * FROM Word WHERE isTrash=0 AND bookId=:bookId AND wordName LIKE :query")
    fun loadWordTuplesWithPaging(bookId:Long, query: String): DataSource.Factory<Int, WordTuple>

    //delete
    @Query("DELETE FROM Word WHERE id IN (:wordId)")
    suspend fun deleteWord(vararg wordId: Long): Int

    @Query("UPDATE Word SET isTrash = 1 WHERE id IN (:wordId)")
    suspend fun deleteWordToTrash(vararg wordId: Long): Int

    @Query("UPDATE Word SET isTrash = 1 WHERE bookid =:bookId")
    suspend fun deleteBookOwnWord(bookId: Long): Int

    //update
    @Query("UPDATE Word SET isMark = :isMark WHERE id =:wordId")
    suspend fun updateMarkWord(wordId:Long, isMark: Boolean): Int
}