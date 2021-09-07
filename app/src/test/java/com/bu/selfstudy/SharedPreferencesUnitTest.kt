package com.bu.selfstudy

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.bu.selfstudy.tool.getSharedPreferences
import com.bu.selfstudy.tool.setSharedPreferences
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 *
 */
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class SharedPreferencesUnitTest {
    val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun settingAndGetting() {
        setSharedPreferences("unit_test_file", context){
            putString("name","toeic")
            putLong("id", 6L)
        }

        getSharedPreferences("unit_test_file", context).run {
            assertThat(getString("name", "")).isEqualTo("toeic")
            assertThat(getLong("id", 0)).isEqualTo(6L)
        }
    }
}