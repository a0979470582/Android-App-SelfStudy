package com.bu.selfstudy.tools

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import androidx.fragment.app.Fragment
import com.bu.selfstudy.SelfStudyApplication

fun Fragment.closeKeyboard(){
    requireActivity().currentFocus?.let{
        (requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply{
            hideSoftInputFromWindow(it?.windowToken, HIDE_NOT_ALWAYS)
        }
    }
}