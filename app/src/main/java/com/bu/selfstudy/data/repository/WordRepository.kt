package com.bu.selfstudy.data.repository

import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import com.bu.selfstudy.data.dao.WordDao
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.network.SelfStudyNetwork
import com.bu.selfstudy.data.network.YahooService
import com.bu.selfstudy.tool.showToast
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


    //insert
    suspend fun insertWord(vararg word: Word) = withContext(Dispatchers.IO){
        wordDao.insert(*word)
    }

    //update
    suspend fun updateWord(vararg word: Word) = withContext(Dispatchers.IO){
        wordDao.update(*word)
    }

    suspend fun updateMark(wordId: Long, isMark: Boolean) = withContext(Dispatchers.IO){
        wordDao.updateMark(wordId, isMark)
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
        wordDao.delete(*wordId)
    }
}