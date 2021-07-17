package com.bu.selfstudy.data.network

import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.tool.log
import okhttp3.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import java.lang.Exception

object YahooService {
    private const val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36"
    private const val api = "https://tw.dictionary.search.yahoo.com/search?p="

    fun getWord(wordName: String): Word{
        val url = api + wordName

        val document = Jsoup.connect(url).ignoreContentType(true)
                .ignoreHttpErrors(true).userAgent(userAgent).get()

        val word = Word()

        word.dictionaryPath = url

        document.select("div.dictionaryWordCard").let{

            if(it.isEmpty())
                return word

            word.wordName = it.select("span.fz-24").first().text()
            //word.pronunciation = it.toTextList("li.d-ib span").joinToString(" ")
            word.pronunciation = it.toTextList("li.d-ib span").first()
                    .replace("KK[", "/ ")
                    .replace("]", " /")
            word.translation = it.toTextList("div.dictionaryExplanation").joinToString("\n")
            word.variation = it.toTextList("li.ov-a span").joinToString("\n")
            //wordDict["partOfSpeech"] = it.toTextList("div.pos_button")
        }

        document.select("div.grp-tab-content-explanation").let{
            //wordDict["example_partOfSpeech"] = it.toTextList("div.compTitle")
            //wordDict["example"] = mutableListOf<Any>()

            val example = ""
            /*
            it.select("div.compTextList").forEach {listElement->
                val rowDataList = mutableListOf<Any>()
                listElement.select("li.va-top").forEach {rowElement->
                    rowDataList.add(rowElement.toTextList("span"))
                }
                (wordDict["example"] as MutableList<Any>).add(rowDataList)
            }*/
        }

        return word
    }

    private fun Elements.toTextList(cssQuery: String) = this.select(cssQuery).map { it.text() }
    private fun Element.toTextList(cssQuery: String) = this.select(cssQuery).map { it.text() }

    fun getAudioResponse(wordName: String): Response? {
        val audioUrlFemale = "https://s.yimg.com/bg/dict/dreye/live/f/${wordName}.mp3"
        val audioUrlMale = "https://s.yimg.com/bg/dict/dreye/live/m/${wordName}.mp3"

        getAudioResponseInternal(audioUrlFemale)?.let {
            return it
        }
        getAudioResponseInternal(audioUrlMale)?.let {
            return it
        }

        return null
    }
    private fun getAudioResponseInternal(url: String): Response?{
        try{
            val client = OkHttpClient()//建立一個OkHttpClient對象
            val request = Request.Builder()//一個HTTP請求, 需要一個Request對象
                    .url(url)//可以加入很多連綴來設定
                    .build()

            client.newCall(request).execute().let {
                if(it.code() == 200)
                    return it
            }
        }catch (e: Exception) {//無網路等情況
            e.printStackTrace()
        }
        return null
    }
}