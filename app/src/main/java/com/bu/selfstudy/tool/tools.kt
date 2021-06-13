package com.bu.selfstudy.tool

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bu.selfstudy.SelfStudyApplication
import com.google.android.material.snackbar.Snackbar

fun Fragment.closeKeyboard(){
    requireActivity().currentFocus?.let{
        (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply{
            hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}

fun <T:Any> T.log(){
    Log.e("debug", this.toString())
}

fun <T:Any> T.showToast(duration:Int = Toast.LENGTH_SHORT){
    Toast.makeText(SelfStudyApplication.context, this.toString(), duration).show()
}

fun View.showSnackbar(text: String, actionText: String? = null, duration: Int = Snackbar.LENGTH_LONG, block: (() -> Unit)? = null) {
    val snackbar = Snackbar.make(this, text, duration)
    if (actionText != null && block != null) {
        snackbar.setAction(actionText) {
            block()
        }
    }
    snackbar.show()
}

fun <T:Any> Any.putBundle(key: String, value: T) = run() {
    val bundle = if(this is Bundle) this else Bundle()
    when(value){
        is Int -> bundle.putInt(key, value)
        is Long -> bundle.putLong(key, value)
        is Short -> bundle.putShort(key, value)
        is Float -> bundle.putFloat(key, value)
        is Double -> bundle.putDouble(key, value)
        is Boolean -> bundle.putBoolean(key, value)
        is String -> bundle.putString(key, value)
        is Byte -> bundle.putByte(key, value)
        is ByteArray -> bundle.putByteArray(key, value)
        is Parcelable -> bundle.putParcelable(key, value)
    }

    return@run bundle
}