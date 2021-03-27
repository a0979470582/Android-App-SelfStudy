package com.bu.selfstudy.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bu.selfstudy.R
import com.bu.selfstudy.showToast
import com.bu.selfstudy.ui.book.BookFragmentDirections
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.timerTask

class LoginFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        "已登入成功, 等待三秒後進入...".showToast()
        GlobalScope.launch {
            delay(3000)
            findNavController().navigate(R.id.action_loginFragment_to_bookFragment)
        }

    }
}