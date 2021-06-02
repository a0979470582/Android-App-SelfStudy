package com.bu.selfstudy.data.local

import androidx.lifecycle.lifecycleScope
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.tool.log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.suspendCoroutine

object LoadLocalBook {
    val bookNameMap = mapOf(
        "toeicData.json" to "多益高頻單字",
        "ieltsData.json" to "雅思核心單字",
        "commonly_use_1000.json" to "最常用1000字",
        "commonly_use_3000.json" to "最常用3000字",
        "high_school_level_1.json" to "高中英文分級Level1",
        "high_school_level_2.json" to "高中英文分級Level2",
        "high_school_level_3.json" to "高中英文分級Level3",
        "high_school_level_4.json" to "高中英文分級Level4",
        "high_school_level_5.json" to "高中英文分級Level5",
        "high_school_level_6.json" to "高中英文分級Level6",
        "junior_school_basic_1200.json" to "國中基礎英文1200字",
        "junior_school_difficult_800.json" to "國中進階英文800字",
        "elementary_school_basic_word.json" to "小學基礎單字"
    )

    suspend fun loadLocalBook(fileName: String): JsonArray = withContext(Dispatchers.IO){
        val inputStream = SelfStudyApplication.context.assets.open("bookData/$fileName")
        val jsonArray = JsonParser().parse(inputStream.reader()).asJsonArray
        return@withContext jsonArray
    }
}