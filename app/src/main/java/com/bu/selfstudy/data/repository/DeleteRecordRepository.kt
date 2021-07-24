package com.bu.selfstudy.data.repository

import com.bu.selfstudy.data.AppDatabase
import com.bu.selfstudy.data.local.LoadSearchAutoComplete
import com.bu.selfstudy.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.lang.Exception

//handle Search Suggestion
object DeleteRecordRepository {
    private val deleteRecordDao = AppDatabase.getDatabase().deleteRecordDao()
    private val wordDao = AppDatabase.getDatabase().wordDao()
    private val bookDao = AppDatabase.getDatabase().bookDao()

    //select
    fun loadDeleteRecordBook() = deleteRecordDao.loadDeleteRecordBook()
    fun loadDeleteRecordWord() = deleteRecordDao.loadDeleteRecordWord()


    //insert
    suspend fun insertDeleteRecordBook(vararg record: DeleteRecordBook) = withContext(Dispatchers.IO){
        deleteRecordDao.insertDeleteRecordBook(*record)
    }

    suspend fun insertDeleteRecordWord(vararg record: DeleteRecordWord) = withContext(Dispatchers.IO){
        deleteRecordDao.insertDeleteRecordWord(*record)
    }

    //logic
    suspend fun handleBookTrash(bookId: Long, isTrash: Boolean) = withContext(Dispatchers.IO){
        if(isTrash){
            val book = bookDao.loadBook(bookId).first()
            insertDeleteRecordBook(DeleteRecordBook(
                    bookId = bookId,
                    bookName = book.bookName,
                    bookColorInt = book.colorInt,
                    bookSize = book.size
            ))
        }else{
            deleteRecordDao.removeDeleteRecordBook(bookId)
        }
    }

    suspend fun handleWordTrash(vararg wordId: Long, isTrash: Boolean) = withContext(Dispatchers.IO){
        if(isTrash){
            insertDeleteRecordWord(
                    *wordId.map {
                        val word = wordDao.loadWord(it).first()
                        DeleteRecordWord(
                                wordId = it,
                                wordName = word.wordName,
                                bookId = word.bookId,
                                bookName = bookDao.loadBook(word.bookId).first().bookName
                                )
                    }.toTypedArray()
            )
        }else{
            deleteRecordDao.removeDeleteRecordWord(wordId=wordId)
        }
    }

    /**
        book:
        var bookId: Long,
        var bookName: String="",
        var bookSize: Int=0,
        var bookColorInt:Int=0,

        word:
        var wordId:Long,
        var wordName: String="",
        var bookId: Long,
        var bookName: String="",
        var theBookDeleted: Boolean=false,
     */
    suspend fun refreshDeleteRecord(deleteRecord: DeleteRecord) = withContext(Dispatchers.IO){
        when(deleteRecord){
            is DeleteRecordBook->{
                //已經刪除的Book其名稱, 單字數, 顏色不會更改, 無須更新
            }
            is DeleteRecordWord->{
                //由於單字所屬Book可能更名, 或被刪除, 需要更新資料
                //wordName不會在回收桶狀態下更改
                val book = bookDao.loadBook(deleteRecord.bookId).firstOrNull()
                val theBookDeleted = (book==null)
                var needUpdate = false

                if(deleteRecord.theBookDeleted != theBookDeleted) {
                    deleteRecord.theBookDeleted = theBookDeleted
                    needUpdate = true
                }

                if(book != null && deleteRecord.bookName!=book.bookName) {
                    deleteRecord.bookName = book.bookName
                    needUpdate = true
                }

                if(needUpdate){
                    deleteRecordDao.updateDeleteRecordWord(deleteRecord)
                }
            }
            else -> throw Exception("don't use DeleteRecord")
        }
    }
}