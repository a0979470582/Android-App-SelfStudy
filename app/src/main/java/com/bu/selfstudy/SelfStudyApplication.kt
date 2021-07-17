package com.bu.selfstudy

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import androidx.annotation.RequiresApi

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