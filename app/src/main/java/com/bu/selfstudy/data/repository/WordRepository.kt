package com.bu.selfstudy.data.repository

import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import com.bu.selfstudy.data.dao.WordDao
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.network.SelfStudyNetwork
import com.bu.selfstudy.data.network.YahooService
import com.bu.selfstudy.tool.log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okio.Okio
import java.io.File


object WordRepository {
    private val wordDao = getDatabase().wordDao()

    val SortStateEnum = WordDao.Companion

    //select
    fun loadOneWord(wordId: Long) = wordDao.loadOneWord(wordId)

    fun loadBookWords(
            bookId: Long,
            sortState: Int=SortStateEnum.OLDEST,
            onlyMark: Boolean=false
    ) = wordDao.loadBookWords(
            bookId,
            sortState,
            if(onlyMark) listOf(1) else listOf(0, 1)
    )

    fun loadMarkWords() = wordDao.loadMarkWords()


    //insert
    suspend fun insertWord(vararg word: Word) = withContext(Dispatchers.IO){
        wordDao.insert(*word).also {
            BookRepository.updateBookSize()
        }
    }

    //update
    suspend fun updateWord(vararg word: Word) = withContext(Dispatchers.IO){
        wordDao.update(*word)
    }

    suspend fun updateMark(vararg wordId: Long, isMark: Boolean) = withContext(Dispatchers.IO){
        if(wordId.size > 900){
            return@withContext wordDao.updateMarkBigSize(*wordId, isMark = isMark)
        }else{
            return@withContext wordDao.updateMark(*wordId, isMark = isMark)
        }
    }

    suspend fun copyWord(wordIdList: List<Long>, bookId: Long) = withContext(Dispatchers.IO){
        val newWordList = wordIdList.map {
            wordDao.loadOneWord(it).first().also {
                it.bookId = bookId
                it.id = 0L
            }
        }

        val size = wordDao.insert(*newWordList.toTypedArray()).size
        BookRepository.updateBookSize(bookId)
        return@withContext size
    }

    suspend fun moveWord(wordIdList: List<Long>, bookId: Long)= withContext(Dispatchers.IO){
        if(wordIdList.size > 900){
            return@withContext wordDao.updateBookIdBigSize(*wordIdList.toLongArray(), bookId=bookId).also {
                BookRepository.updateBookSize(bookId)
            }
        }else{
            return@withContext wordDao.updateBookId(*wordIdList.toLongArray(), bookId=bookId).also {
                BookRepository.updateBookSize(bookId)
            }
        }
    }

    //network
    suspend fun getYahooWord(wordName: String): Result<Word> = withContext(Dispatchers.IO){
        kotlin.runCatching {
            SelfStudyNetwork.getYahooWord(wordName)
        }
    }

    suspend fun downloadAudio(wordId: Long)
    = withContext(Dispatchers.IO){
        val wordName = wordDao.loadOneWord(wordId).first().wordName

        YahooService.getAudioResponse(wordName)?.let { response->
            val file = File(SelfStudyApplication.context.filesDir, "${wordName}.mp3")
            val sink = Okio.buffer(Okio.sink(file))

            response.body()?.let { body->
                sink.writeAll(body.source())
                sink.close()
                wordDao.updateAudioFilePath(wordId, file.name)
            }
        }
    }

    //delete
    suspend fun delete(vararg wordId: Long) = withContext(Dispatchers.IO){
        if(wordId.size > 900){
            return@withContext wordDao.deleteBigSize(*wordId).also {
                BookRepository.updateBookSize()
            }
        }else{
            return@withContext wordDao.delete(*wordId).also {
                BookRepository.updateBookSize()
            }

        }
    }
}