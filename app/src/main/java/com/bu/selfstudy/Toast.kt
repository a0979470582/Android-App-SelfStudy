package com.bu.selfstudy

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
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
fun main() = runBlocking {
    val channel = Channel<Int>()
    launch {
        repeat(5){
            delay(1000)
            channel.send(it)
            if(it==4)channel.close()
        }
    }
    for(n in channel){
        println(n)
    }
    println("done")
}