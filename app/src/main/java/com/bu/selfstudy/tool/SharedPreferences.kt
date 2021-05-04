package com.bu.selfstudy.tool

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.bu.selfstudy.SelfStudyApplication

fun setSharedPreferences(fileName:String, block: SharedPreferences.Editor.()->Unit){
    SelfStudyApplication.context.getSharedPreferences(
        fileName, Context.MODE_PRIVATE
    ).edit {
        block()
    }
}
fun getSharedPreferences(fileName:String)
        = SelfStudyApplication.context.getSharedPreferences(
    fileName, Context.MODE_PRIVATE)