package com.bu.selfstudy.data.local

import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.tool.log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LoadLocalBook {

    suspend fun loadBookData(bookName: String): List<Word> = withContext(Dispatchers.IO){
        val inputStream = SelfStudyApplication.context.assets.open("bookData/${bookName}.json")
        val jsonArray = JsonParser().parse(inputStream.reader()).asJsonArray
        val wordList = mutableListOf<Word>()

        jsonArray.forEach { jsonElement -> (jsonElement as JsonObject).let {
            val wordName = if(it.getAsJsonArray("wordName").size()>0)
                it.getAsJsonArray("wordName")[0].asString else ""

            val pronunciation = if(it.getAsJsonArray("pronunciation").size()>0)
                it.getAsJsonArray("pronunciation")[0].asString
                        .replace("KK[", "/ ")
                        .replace("]"," /") else ""

            //val audioPath = if (it.get("audioPath").isJsonArray) ""
            //else it.get("audioPath").asString

            var translation = ""
            for(i in 0 until it.getAsJsonArray("partOfSpeech").size()){
                if(i != 0)
                    translation = translation.plus("\n")
                translation = translation.plus(it.getAsJsonArray("partOfSpeech")[i].asString)
                translation = translation.plus(" ")
                translation = translation.plus(it.getAsJsonArray("translation")[i].asString)
            }

            var variation = ""
            if(it.getAsJsonArray("variation_v").size()>0)
                variation = variation.plus(it.getAsJsonArray("variation_v")[0].asString)

            if(it.getAsJsonArray("variation_n").size()>0) {
                if (variation!="")
                    variation = variation.plus("\n")
                variation = variation.plus(it.getAsJsonArray("variation_n")[0].asString)
            }

            var example = ""
            for(i in 0 until it.getAsJsonArray("example_partOfSpeech").size()){
                if(i != 0)
                    example = example.plus("\n\n")
                example = example.plus(it.getAsJsonArray("example_partOfSpeech")[i].asString)
                example = example.plus("\n")
                example = example.plus(it.getAsJsonArray("example")[i].asString)
            }

            wordList.add(Word(
                    bookId = 0,
                    wordName = wordName,
                    pronunciation = pronunciation,
                    translation = translation,
                    variation = variation,
                    example = example
            ))
        }}
        return@withContext wordList
    }

    suspend fun loadNames() = withContext(Dispatchers.IO){
        return@withContext SelfStudyApplication.context.assets
                .list("bookData")?.map {
                    it.replace(".json", "")
                }
    }
}