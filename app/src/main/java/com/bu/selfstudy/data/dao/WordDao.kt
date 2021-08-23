package com.bu.selfstudy.data.dao

import androidx.room.*
import androidx.room.util.StringUtil
import androidx.sqlite.db.SupportSQLiteStatement
import com.bu.selfstudy.data.AppDatabase
import com.bu.selfstudy.data.model.Word
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.Callable
import kotlin.coroutines.Continuation

/**
 * 注意一個Statement後面所帶變量最多999
 */
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
    fun loadOneWord(wordId: Long): Flow<Word>

    //all book, query
    @Query("SELECT wordName FROM Word WHERE wordName>=:query AND wordName<=:query||'z'")
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


    @Query("SELECT * FROM Word WHERE isMark = 1")
    fun loadMarkWords(): Flow<List<Word>>


    //update
    @Query("UPDATE Word SET isMark = :isMark WHERE id IN (:wordId)")
    suspend fun updateMark(vararg wordId: Long, isMark: Boolean): Int

    suspend fun updateMarkBigSize(vararg wordId: Long, isMark: Boolean): Int{
        return doUpdateDelete(
                wordId = wordId,
                baseSql = "UPDATE Word SET isMark=${if(isMark) 1 else 0} WHERE id IN "
        )
    }


    @Query("UPDATE Word SET audioFilePath = :filePath WHERE id = :wordId")
    suspend fun updateAudioFilePath(wordId: Long, filePath: String): Int

    @Query("UPDATE Word SET bookId = :bookId WHERE id IN (:wordId)")
    suspend fun updateBookId(vararg wordId: Long, bookId: Long): Int

    suspend fun updateBookIdBigSize(vararg wordId: Long, bookId: Long): Int{
        return doUpdateDelete(
                wordId = wordId,
                baseSql = "UPDATE Word SET bookId=${bookId} WHERE id IN "
        )
    }

    //delete
    @Query("DELETE FROM Word WHERE id IN (:wordId)")
    suspend fun delete(vararg wordId: Long): Int

    /** 同一個任務應在一個Transaction完成, 否則先完成的子任務要返回資料庫現況時,
     *  會受下一個子任務影響 */
    suspend fun deleteBigSize(vararg wordId: Long): Int {
        return doUpdateDelete(
                wordId = wordId,
                baseSql = "DELETE FROM Word WHERE id IN "
        )
    }

    //tool

    //return 0..900, 900..1800, 1800..2136(exclusive)
    private fun chopArray(idList: LongArray): ArrayList<LongArray>{
        val count = 900
        val arrayList = ArrayList<LongArray>()
        if(idList.size > count) {
            var startIndex = 0

            while (true) {
                if (startIndex + count < idList.size) {
                    arrayList.add(idList.copyOfRange(startIndex, startIndex + count))
                    startIndex += count
                } else {
                    arrayList.add(idList.copyOfRange(startIndex, idList.size))
                    break
                }
            }
        }

        return arrayList
    }

    private fun doUpdateDelete(wordId: LongArray, baseSql: String): Int{
        val db: AppDatabase = AppDatabase.getDatabase()

        db.beginTransaction()
        try {
            val result = chopArray(wordId).map { idList->
                val stringBuilder = StringBuilder().append(baseSql).append(" (")
                for (i in idList.indices) {
                    if(i == idList.lastIndex)
                        stringBuilder.append("?")
                    else
                        stringBuilder.append("?,")
                }
                stringBuilder.append(")")

                val stmt: SupportSQLiteStatement = db.compileStatement(stringBuilder.toString())
                var index = 1
                for(id in idList){
                    stmt.bindLong(index, id)
                    index++
                }

                val result = stmt.executeUpdateDelete()
                result
            }.reduce { acc, unit -> acc + unit }

            db.setTransactionSuccessful()
            return result
        }finally {
            db.endTransaction()
        }
    }
}