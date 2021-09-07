package com.bu.selfstudy.tool

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.bu.selfstudy.SelfStudyApplication

fun setSharedPreferences(
    fileName:String,
    context: Context=SelfStudyApplication.context,
    block: SharedPreferences.Editor.()->Unit
){
    context.getSharedPreferences(
        fileName, Context.MODE_PRIVATE
    ).edit {
        block()
    }
}
fun getSharedPreferences(
    fileName:String,
    context: Context=SelfStudyApplication.context,
) = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)