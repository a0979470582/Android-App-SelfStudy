package com.bu.selfstudy

import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApplicationTest{

    lateinit var application: SelfStudyApplication

    @Before
    fun setContext(){
        application = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun checkAppContext(){
        assertThat(application.packageName).isEqualTo("com.bu.selfstudy")
    }

    @Test
    fun checkSettingTheme(){
        assertThat(application.getCurrentTheme()).isIn(listOf("day", "night"))

        application.setTheme("day")
        assertThat(application.getCurrentTheme()).isEqualTo("day")
        assertThat(AppCompatDelegate.getDefaultNightMode()).isEqualTo(AppCompatDelegate.MODE_NIGHT_NO)

        application.setTheme("night")
        assertThat(application.getCurrentTheme()).isEqualTo("night")
        assertThat(AppCompatDelegate.getDefaultNightMode()).isEqualTo(AppCompatDelegate.MODE_NIGHT_YES)


        application.setTheme("any error text")
        assertThat(application.getCurrentTheme()).isEqualTo("day")
        assertThat(AppCompatDelegate.getDefaultNightMode()).isEqualTo(AppCompatDelegate.MODE_NIGHT_NO)
    }


}