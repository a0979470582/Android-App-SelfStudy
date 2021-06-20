package com.bu.selfstudy.data.network

import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

//call common logic, use coroutine, try-catch
object SelfStudyNetwork {

    suspend fun getWordPage(wordName: String) = suspendCoroutine<Map<String, *>> { continuation->
        try {
            val wordPage = YahooService.getWordPage(wordName)
            continuation.resume(wordPage)
        }catch (e: Exception){
            continuation.resumeWithException(e)
        }
    }
}