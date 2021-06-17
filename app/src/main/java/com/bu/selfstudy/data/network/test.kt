package com.bu.selfstudy.data.network

import com.bu.selfstudy.tool.log
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.select.Elements

fun main2(){
    val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36"
    val doc = Jsoup
            .connect("https://tw.dictionary.search.yahoo.com/search?p=reserve")
            .userAgent(userAgent)
            .get()
    doc.title().log()
    val lines = doc.select("fz-24 fw-500 c-black lh-24")
    for(line in lines){
        line.log()
    }
}

fun main(){
    runBlocking {
        val doc = Jsoup.connect("https://en.wikipedia.org/").get()
        (doc.title()).log()
        val newsHeadlines: Elements = doc.select("#mp-itn b a")
        for (headline in newsHeadlines) {
            headline.log()
        }
    }
}