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
    //select with one wordId
    @Query("SELECT * FROM Word WHERE isTrash=0 AND id=:wordId")
    fun loadWord(wordId:Long): Flow<Word>
    fun loadDistinctWord(wordId:Long) = loadWord(wordId).distinctUntilChanged()

    //one bookId, query
    @Query("SELECT * FROM Word WHERE isTrash=0 AND bookId=:bookId AND wordName LIKE :query")
    fun loadWords(bookId:Long, query: String): Flow<List<Word>>
    fun loadDistinctWords(bookId:Long, query: String) = loadWords(bookId, query).distinctUntilChanged()

    //all book, query
    @Query("SELECT wordName FROM Word WHERE isTrash=0 AND wordName LIKE :query || '%' ORDER BY wordName")
    fun loadWords(query: String): Flow<List<String>>

    //select with paging3
    @Query("SELECT * FROM Word WHERE isTrash=0 AND bookId=:bookId AND wordName LIKE :query")
    fun loadWordsWithPaging(bookId:Long, query: String): DataSource.Factory<Int, Word>

    @Query("SELECT * FROM Word WHERE isTrash=0 AND bookId=:bookId AND wordName LIKE :query")
    fun loadWordTuplesWithPaging(bookId:Long, query: String): DataSource.Factory<Int, WordTuple>

    //delete
    @Query("DELETE FROM Word WHERE id IN (:wordId)")
    suspend fun deleteWord(vararg wordId: Long): Int

    //update
    @Query("UPDATE Word SET isMark = :isMark WHERE id =:wordId")
    suspend fun updateWordMark(wordId:Long, isMark: Boolean): Int

    @Query("UPDATE Word SET isTrash = :isTrash WHERE id IN (:wordId)")
    suspend fun updateWordIsTrash(vararg wordId: Long, isTrash: Boolean): Int

    @Query("UPDATE Word SET isTrash = :isTrash WHERE bookId =:bookId")
    suspend fun updateWordIsTrash(bookId: Long, isTrash: Boolean): Int

    @Query("UPDATE Word SET audioFilePath = :filePath WHERE id = :wordId")
    suspend fun updateAudioFilePath(wordId: Long, filePath: String): Int
}