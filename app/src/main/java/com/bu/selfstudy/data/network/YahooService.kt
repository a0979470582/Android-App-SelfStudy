package com.bu.selfstudy.data.network

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

//common logic, no coroutine, no try-catch
object YahooService {
    private const val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36"

    fun getWordPage(wordName: String): Map<String, *>{
        val url = "https://tw.dictionary.search.yahoo.com/search?p=${wordName}"
        val audioUrlFemale = "https://s.yimg.com/bg/dict/dreye/live/f/${wordName}.mp3"
        val audioUrlMale = "https://s.yimg.com/bg/dict/dreye/live/m/${wordName}.mp3"

        val document = Jsoup.connect(url).ignoreContentType(true)
                .ignoreHttpErrors(true).userAgent(userAgent).get()

        val wordDict = mutableMapOf<String, Any>()

        document.select("div.dictionaryWordCard").let{
            wordDict["wordName"] = it.select("span.fz-24").first().text()
            wordDict["pronunciation"] = it.toTextList("li.d-ib span")
            wordDict["partOfSpeech"] = it.toTextList("div.pos_button")
            wordDict["translation"] = it.toTextList("div.dictionaryExplanation")
            wordDict["variation"] = it.toTextList("li.ov-a span")
        }

        document.select("div.grp-tab-content-explanation").let{
            wordDict["example_partOfSpeech"] = it.toTextList("div.compTitle")
            wordDict["example"] = mutableListOf<Any>()

            it.select("div.compTextList").forEach {listElement->
                val rowDataList = mutableListOf<Any>()
                listElement.select("li.va-top").forEach {rowElement->
                    rowDataList.add(rowElement.toTextList("span"))
                }
                (wordDict["example"] as MutableList<Any>).add(rowDataList)
            }
        }

        wordDict["audioPath"] = when {
            getAudioPathResponse(audioUrlFemale).statusCode()==200 -> audioUrlFemale
            getAudioPathResponse(audioUrlMale).statusCode()==200 -> audioUrlMale
            else -> ""
        }

        return wordDict
    }


    private fun Elements.toTextList(cssQuery: String) = this.select(cssQuery).map { it.text() }
    private fun Element.toTextList(cssQuery: String) = this.select(cssQuery).map { it.text() }


    private fun getAudioPathResponse(url: String)= Jsoup.connect(url)
            .ignoreContentType(true)
            .ignoreHttpErrors(true)
            .execute()
}