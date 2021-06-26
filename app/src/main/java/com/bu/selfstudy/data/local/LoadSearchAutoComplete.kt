package com.bu.selfstudy.data.local

import com.bu.selfstudy.SelfStudyApplication
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LoadSearchAutoComplete {
    val filenameList = listOf("toeic.txt", "美國小學生基石單字.txt",
            "高中level1.txt", "高中level2.txt", "高中level3.txt",
            "高中level4.txt", "高中level5.txt", "高中level6.txt",
            "國中基礎1200.txt", "國中進階800.txt", "最常用1000.txt",
            "最常用3000.txt", "雅思.txt"
    )

    suspend fun loadData(fileName: String): List<String> = withContext(Dispatchers.IO){
        val inputStream = SelfStudyApplication.context.assets.open("autoCompleteData/$fileName")
        return@withContext inputStream.reader().readLines()
    }
}