package com.bu.selfstudy.data.network

import retrofit2.Retrofit

//usage val appService = ServiceCreator.create(AppService::class.java)
object ServiceCreator {
    private const val BASE_URL = "https://tw.dictionary.search.yahoo.com/"
    private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()

    fun <T> create(serviceClass: Class<T>):T = retrofit.create(serviceClass)
}