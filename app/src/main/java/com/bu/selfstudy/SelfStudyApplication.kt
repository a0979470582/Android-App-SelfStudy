package com.bu.selfstudy

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.bu.selfstudy.data.model.BackupMetadata
import com.bu.selfstudy.tool.log
import com.bu.selfstudy.tool.getSharedPreferences
import com.bu.selfstudy.tool.setSharedPreferences

class SelfStudyApplication : Application() {

    private val sharedPreferencesFileName = "self_study_data"

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        var memberId = 1L
        val backupMetadata = MutableLiveData(BackupMetadata(hasBackup = false))
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        initTheme()
    }


    private fun initTheme(){
        setTheme(getCurrentTheme())
    }

    fun setTheme(themeString: String) {

        setSharedPreferences(sharedPreferencesFileName){
            putString("themeString", themeString)
        }

        val mode = when(themeString){

            "day"-> AppCompatDelegate.MODE_NIGHT_NO
            "night"-> AppCompatDelegate.MODE_NIGHT_YES

            else-> AppCompatDelegate.MODE_NIGHT_NO
        }

        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun getCurrentTheme(): String{
        val themeString = getSharedPreferences(sharedPreferencesFileName)
                .getString("themeString", "day")

        return if(themeString == "night")
            "night"
        else
            "day"
    }


    /*待更新, 暫時以APP內的主題設置為主
    fun getSystemTheme(): String{
        return when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_NO -> "day"
            Configuration.UI_MODE_NIGHT_YES -> "night"
            Configuration.UI_MODE_NIGHT_UNDEFINED->"day"
            else-> "day"
        }
    }*/

}