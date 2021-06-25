package com.bu.selfstudy.data.network

import com.bu.selfstudy.data.model.Word
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

//call common logic, use coroutine, try-catch
object SelfStudyNetwork {

    suspend fun getYahooWord(wordName: String) = suspendCoroutine<Word> { continuation->
        try {
            val word = YahooService.getWord(wordName)
            if(word.wordName.isNullOrBlank())
                continuation.resumeWithException(RuntimeException("無此單字"))
            continuation.resume(word)
        }catch (e: Exception){
            continuation.resumeWithException(e)
        }
    }
}