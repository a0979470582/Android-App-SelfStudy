package com.bu.selfstudy.data.local

import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.LocalBook
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.tool.log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object LoadLocalBook {

    /**
     * json檔的資料來源中wordName和translation一定存在, 其餘欄位可能是空字串("")值

        {
        "count": 396,
        "data": [
                {
                    "audio_url": "https://s.yimg.com/bg/dict/dreye/live/f/able.mp3",
                    "example": "adj.形容詞\n1. 能，可，會[F][+to-v]\nI am afraid I won't be able to visit you on Saturday. 恐怕我無法在星期六來拜訪您了。\n\n2. 有能力的；能幹的\nHe is an able lawyer. 他是一位能幹的律師。\n\n",
                    "pronunciation": "/ ˋeb! /",
                    "synonyms": "同義詞\na. 能，會；有能力的\nskillful, capable, efficient, proficient, qualified, having power\n\n反義詞\n「a. 能；有能力的；能幹的」的反義字\nunable, incapable",
                    "translation": "adj. 能，可，會[F][+to-v]。有能力的。能幹的 ",
                    "variation": "比較級：abler\n最高級：ablest",
                    "word_name": "able"
                },{...},{...},....{...}
            ]
        }

     *給予book名稱, 從assets資料夾解析json檔, 返回單字列表
      */
    suspend fun loadBookData(file: File): List<Word> = withContext(Dispatchers.IO){

        val jsonElement = JsonParser().parse(
            file.inputStream().reader()
        )

        val wordList =  java.util.ArrayList<Word>(
            jsonElement.asJsonObject["count"].asInt
        )

        jsonElement.asJsonObject["data"].asJsonArray.forEach { wordElement ->
            val wordObj = (wordElement as JsonObject)

            wordList.add(Word(
                    bookId = 0,
                    wordName = wordObj["word_name"].asString,
                    pronunciation = wordObj["pronunciation"].asString,
                    translation = wordObj["translation"].asString,
                    variation = wordObj["variation"].asString,
                    example = wordObj["example"].asString,
                    audioFilePath = wordObj["audio_url"].asString,
                    synonyms = wordObj["synonyms"].asString
            ))
        }
        return@withContext wordList
    }

    /**
     * 返回可下載的Book資訊
     */
    suspend fun loadNames() = withContext(Dispatchers.IO){

        val jsonElement = JsonParser().parse(
                SelfStudyApplication.context.assets
                        .open("bookData/local_book_data.json").reader()
        )

        val arrayList = ArrayList<LocalBook>()

        jsonElement.asJsonArray.forEach { bookElement->
            bookElement.asJsonObject.let { bookObj->
                arrayList.add(
                    LocalBook(
                        bookName = bookObj["bookName"].asString,
                        size = bookObj["size"].asString,
                        fileSize = bookObj["fileSize"].asString,
                        explanation = bookObj["explanation"].asString,
                    )
                )
            }
        }

        return@withContext arrayList

    }
}