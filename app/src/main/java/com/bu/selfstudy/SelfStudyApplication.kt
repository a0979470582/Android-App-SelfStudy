package com.bu.selfstudy

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class SelfStudyApplication : Application() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        var memberId = 1L
    }
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}