package com.bu.selfstudy.data.dao

import androidx.paging.DataSource
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.model.WordTuple
import com.bu.selfstudy.data.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface WordDao : BaseDao<Word>{
    companion object{
        const val OLDEST = 0
        const val NEWEST = 1
        const val AZ = 2
        const val ZA = 3
    }

    //select with one wordId
    @Query("SELECT * FROM Word WHERE id=:wordId")
    fun loadOneWord(wordId:Long): Flow<Word>

    //all book, query
    @Query("SELECT wordName FROM Word WHERE wordName LIKE :query || '%'")
    fun searchWords(query: String): Flow<List<String>>


    @Query("""SELECT * FROM Word WHERE bookId=:bookId AND isMark in (:markList) Order by
        CASE WHEN :sortState=0 THEN timestamp END ASC,
        CASE WHEN :sortState=1 THEN timestamp END DESC,
        CASE WHEN :sortState=2 THEN wordName END ASC,
        CASE WHEN :sortState=3 THEN wordName END DESC
        """)
    fun loadBookWords(
            bookId: Long,
            sortState: Int,
            markList: List<Int>
    ): Flow<List<Word>>

    //update
    @Query("UPDATE Word SET isMark = :isMark WHERE id =:wordId")
    suspend fun updateMark(wordId:Long, isMark: Boolean): Int

    @Query("UPDATE Word SET audioFilePath = :filePath WHERE id = :wordId")
    suspend fun updateAudioFilePath(wordId: Long, filePath: String): Int

    //delete
    @Query("DELETE FROM Word WHERE id IN (:wordId)")
    suspend fun delete(vararg wordId: Long): Int


}