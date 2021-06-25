package com.bu.selfstudy.data.network

import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.tool.log
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

//common logic, no coroutine, no try-catch
object YahooService {
    private const val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36"

    fun getWord(wordName: String): Word{
        val url = "https://tw.dictionary.search.yahoo.com/search?p=${wordName}"
        val audioUrlFemale = "https://s.yimg.com/bg/dict/dreye/live/f/${wordName}.mp3"
        val audioUrlMale = "https://s.yimg.com/bg/dict/dreye/live/m/${wordName}.mp3"

        val document = Jsoup.connect(url).ignoreContentType(true)
                .ignoreHttpErrors(true).userAgent(userAgent).get()

        val word = Word(bookId = 0, wordName = "")
        //val wordDict = mutableMapOf<String, Any>()
        word.dictionaryPath = url

        document.select("div.dictionaryWordCard").let{
            if(it.isEmpty())
                return word
            word.wordName = it.select("span.fz-24").first().text()
            word.pronunciation = it.toTextList("li.d-ib span").joinToString(" ")
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

        word.audioPath = when {
            getAudioPathResponse(audioUrlFemale).statusCode()==200 -> audioUrlFemale
            getAudioPathResponse(audioUrlMale).statusCode()==200 -> audioUrlMale
            else -> ""
        }

        return word
    }


    private fun Elements.toTextList(cssQuery: String) = this.select(cssQuery).map { it.text() }
    private fun Element.toTextList(cssQuery: String) = this.select(cssQuery).map { it.text() }


    private fun getAudioPathResponse(url: String)= Jsoup.connect(url)
            .ignoreContentType(true)
            .ignoreHttpErrors(true)
            .execute()
}