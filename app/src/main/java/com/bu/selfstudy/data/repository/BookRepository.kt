package com.bu.selfstudy.data.repository

import androidx.lifecycle.liveData
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Member
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import com.bu.selfstudy.data.local.LoadLocalBook
import com.bu.selfstudy.tool.log
import com.bu.selfstudy.tool.showToast
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BookRepository {
    val bookDao = getDatabase().bookDao()

    fun loadBooks(memberId:Long=SelfStudyApplication.memberId) = bookDao.loadDistinctBooks(memberId)


    suspend fun insertBook(vararg book: Book) = withContext(Dispatchers.IO){
        bookDao.insert(*book)
    }
    suspend fun updateBook(vararg book: Book) = withContext(Dispatchers.IO){
        bookDao.update(*book)
    }
    suspend fun deleteBook(vararg bookId: Long) = withContext(Dispatchers.IO){
        bookDao.delete(*bookId)
    }
    suspend fun deleteBookToTrash(bookId: Long) = withContext(Dispatchers.IO){
        bookDao.deleteBookToTrash(bookId).also {
            WordRepository.deleteOneBookToTrash(bookId)
        }
    }

    suspend fun updateBookSize(bookId: Long) = withContext(Dispatchers.IO){
        bookDao.updateBookSize(bookId)
    }

    suspend fun insertLocalBook(fileName: String) = withContext(Dispatchers.IO) {
        val jsonArray: JsonArray = LoadLocalBook.loadLocalBook(fileName)
        val bookId = insertBook(Book(memberId = SelfStudyApplication.memberId,
                bookName = LoadLocalBook.bookNameMap[fileName]?:"新題庫"))[0]

        val wordList = mutableListOf<Word>()
        jsonArray.forEach { jsonElement -> (jsonElement as JsonObject).let {
            val wordName = if(it.getAsJsonArray("wordName").size()>0)
                               it.getAsJsonArray("wordName")[0].asString
                           else ""

            val pronunciation = if(it.getAsJsonArray("pronunciation").size()>0)
                                    it.getAsJsonArray("pronunciation")[0].asString
                                else ""

            val audioPath = if (it.get("audioPath").isJsonArray) ""
                            else it.get("audioPath").asString

            var translation = ""
            for(i in 0 until it.getAsJsonArray("partOfSpeech").size()){
                if(i != 0)
                    translation = translation.plus("\n")
                translation = translation.plus(it.getAsJsonArray("partOfSpeech")[i].asString)
                translation = translation.plus(" ")
                translation = translation.plus(it.getAsJsonArray("translation")[i].asString)
            }

            var variation = ""
            if(it.getAsJsonArray("variation_n").size()>0)
                variation = variation.plus(it.getAsJsonArray("variation_n")[0].asString)

            if(it.getAsJsonArray("variation_v").size()>0) {
                if (variation!="")
                    variation = variation.plus("\n")
                variation = variation.plus(it.getAsJsonArray("variation_v")[0].asString)
            }

            var example = ""
            for(i in 0 until it.getAsJsonArray("example_partOfSpeech").size()){
                if(i != 0)
                    example = example.plus("\n")
                example = example.plus(it.getAsJsonArray("example_partOfSpeech")[i].asString)
                example = example.plus("\n")
                example = example.plus(it.getAsJsonArray("example")[i].asString)
            }

            wordList.add(Word(
                    bookId = bookId,
                    wordName = wordName,
                    pronunciation = pronunciation,
                    audioPath = audioPath,
                    translation = translation,
                    variation = variation,
                    example = example
            ))
        }}
        val wordIdList = WordRepository.insertWord(*wordList.toTypedArray(), bookId = bookId)
        return@withContext if(wordIdList.isNotEmpty())
             "新增成功"
        else
            "新增失敗"
    }
}