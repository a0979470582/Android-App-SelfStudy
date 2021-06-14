package com.bu.selfstudy.data.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

//https://tw.dictionary.search.yahoo.com/search?p=test
interface YahooDictionaryService {
    @GET("search")
    fun getWordPage(@Query("p") wordName:String): Call<ResponseBody>
}