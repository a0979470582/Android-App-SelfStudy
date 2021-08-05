package com.bu.selfstudy.data.network

import com.bu.selfstudy.data.model.Word
import okhttp3.*
import org.jsoup.Jsoup
import java.lang.Exception

object YahooService {
    /**代理者訊息是為了載入電腦版網頁*/
    private const val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36"
    private const val api = "https://tw.dictionary.search.yahoo.com/search?p="

    fun getWord(wordName: String): Word{
        val url = api + wordName

        val document = Jsoup
                .connect(url)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .userAgent(userAgent)
                .get()

        val word = Word()

        word.dictionaryPath = url

        /**單字卡區域, 包含的資訊有: 單字名, 音標, 解釋, 變化形 */
        document.select("div.dictionaryWordCard").let{

            if(it.isEmpty())
                return word

            word.wordName = it
                    .select("h3.title span.fz-24")
                    .first()
                    ?.text()
                    ?:""

            word.pronunciation = it
                    .select("li.d-ib span")
                    .first()
                    ?.text()
                    ?.replace("KK", "")//以中文來查詢字典, 其音標並非KK, 所以KK和[不能相連
                    ?.replace("[", "/ ")
                    ?.replace("]", " /")
                    ?:""

            word.translation = it
                    .select("div.dictionaryWordCard div.compList.p-rel li")
                    .map {
                        element -> element
                            .select("div")
                            .joinToString(" "){
                                it.text()
                                .replace("；", "。")
                                .replace("（", "(")
                                .replace("）", ")")
                            }
                            //n.[C] 試驗；測試；化驗；化驗法；化驗劑
                    }
                    .joinToString("\n")
                    .trimEnd('\n')

            //保留html中的<b>元素, 才知道需加入換行符\n的位置
            word.variation = it
                    .select("li.ov-a span")
                    .joinToString("") { element -> element.html() }
                    .split("</b>")// ex: [式：<b>red,  詞：<b>red,  詞：<b>rng, 名數：<b>res, ]
                    .joinToString("\n") { element-> element.trim() } //去除三態動詞後兩項之開頭空白
                    .replace("<b>", "")
                    .trimEnd('\n')

        }

        /**
         * 此處為例句的區域, 它包含三層嵌套, 範例如下:
         * 詞性列表 -> 解釋列表 -> 例句列表
            vt.及物動詞
                1.儲備，保存；保留[（+for）]
                    These seats are reserved for special guests. 這些座位是為特別來賓保留的。
                    We will reserve the money; we may need it later. 我們將把這筆錢存起來，也許以後用得著。
                2.預約，預訂
                    They have reserved rooms at a hotel. 他們已預訂了旅館房間。
                    I have reserved a table at the restaurant. 我已在飯店預訂了一桌菜。
            n.名詞
                1.儲備（物）；儲備金；保留（物）；儲藏量[C][U][（+of）]
                    The old man kept a large reserve of firewood for cold weather. 這位老人貯存了大量的柴薪以備天冷時用。
                2.保留地；保護區；禁獵區
         */

        /**
         * 結構問題先擱著
         * 1. 若採用三層嵌套, 首先發送Word和ExamplePartOfSpeech的關係表到介面端,
         * 接著介面端請求對應的ExamplePartOfSpeech和ExampleExplain關係表, 接著還要再往一下層,
         * 總共三層, 可能造成介面反應很慢
         *
         * 2. 若將三層平級化, 用一個欄位紀錄個別的層級, 那必須自行管理各層級的關聯性
         */
        document.select("div.grp-tab-content-explanation").let{

            // [vt. 及物動詞, n. 名詞]
            val partOfSpeechList = it
                    .select("div.compTitle")
                    .map {it.select("span").text() }

            word.example = it
                    .select("div.grp-tab-content-explanation div.compTextList")
                    .map{
                        it.select("li").map {
                            var explain = ""
                            explain += it.select("span.fw-xl").text()+" "
                            explain += it.select("span.d-i").text()
                            it.select("span.fc-2nd").forEach {
                                explain += "\n" + it.text()
                            }
                            explain+"\n"
                        }.joinToString("\n")
                    }.mapIndexed{ index, text->
                        partOfSpeechList.get(index)+"\n"+text
                    }.joinToString("\n")

        }

        /**
         * 正反義區
         */
        document.select("div.grp-tab-content-synonyms").let { elements->

            if(elements.isEmpty())
                return@let

            word.synonyms = elements
                    .first()
                    .children()
                    .joinToString("\n") {
                        if (it.hasClass("compDlink"))
                            it.select("li").joinToString(""){
                                it.text() }.plus("\n")
                        else
                            it.select("span").text()
                    }
                    .trimEnd('\n')

        }



        /**
        從html中找到這一段, 它是JS語法
        https:\/\/s.yimg.com\/bg\/dict\/dreye\/live\/f\/reserve.mp3

        先紀錄音樂檔的位址, f=女聲, m=男聲, 每一個單字的音檔只有其中一種
        https://s.yimg.com/bg/dict/dreye/live/f/${wordName}.mp3
        https://s.yimg.com/bg/dict/dreye/live/m/${wordName}.mp3
         */


        word.wordName?.let{ wordName->
            Regex("https:.{10,100}${wordName}.mp3").find(document.html())?.let { result->
                word.audioFilePath = result.value.replace("\\", "")
            }

        }


        return word
    }


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

