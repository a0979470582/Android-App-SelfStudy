package com.bu.selfstudy

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bu.selfstudy.logic.Repository
import com.bu.selfstudy.logic.model.Word
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.lang.Thread.sleep
import java.sql.Time
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.system.measureTimeMillis

fun String.showToast(duration:Int = Toast.LENGTH_SHORT){
    Toast.makeText(SelfStudyApplication.context, this, duration).show()
}
fun Int.showToast(duration:Int = Toast.LENGTH_SHORT){
    Toast.makeText(SelfStudyApplication.context, this, duration).show()
}
fun main(){
    val wordList = ArrayList<Long>(mutableListOf(1,2,3,4,5,6))
    val wordList2 = ArrayList<Long>(mutableListOf(5,6))
    val newWordList:List<Long> = wordList
            .filter { wordList2.contains(it) }
            .map{
                it->it*10
            }
    println(wordList)
    println(wordList2)
    println(newWordList)
}