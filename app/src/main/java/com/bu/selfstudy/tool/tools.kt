package com.bu.selfstudy.tool

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.ui.main.MainActivity
import com.google.android.material.snackbar.Snackbar

fun Fragment.closeKeyboard(){
    requireActivity().currentFocus?.let{
        (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply{
            hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}
fun Fragment.openKeyboard(){
    (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply{
        toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }
}

fun <T:Any> T.log(){
    Log.e("debug", this.toString())
}

/**
 * Toast無法自適應主題, 建議改用Snackbar
 */
fun <T:Any> T.showToast(context: Context = SelfStudyApplication.context, duration:Int = Toast.LENGTH_SHORT){
    Toast.makeText(context, this.toString(), duration).show()
}

fun <T:Any> T.showOnlyTextSnackbar(
        view: View,
        duration: Int = Snackbar.LENGTH_SHORT,
        anchorView: View? = null,
){
    if(anchorView == null)
        Snackbar.make(view, this.toString(), duration).show()
    else
        Snackbar.make(view, this.toString(), duration)
                .setAnchorView(anchorView)
                .show()
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

fun testTaskTime(block: (() -> Unit)){
    val startTime = System.currentTimeMillis()
    block()
    val endTime = System.currentTimeMillis()
    val result = endTime - startTime
    result.log()
}


fun hasNetwork(): Boolean {
    val connectivityManager = SelfStudyApplication.context.getSystemService(
            Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        connectivityManager.activeNetwork != null
    } else {
        val networkInfo = connectivityManager.activeNetworkInfo
        networkInfo != null && networkInfo.isAvailable
    }
}

fun Fragment.setNewToolbar(toolbar: Toolbar){
    setHasOptionsMenu(true)

    (activity as? MainActivity)?.let {
        it.setSupportActionBar(toolbar)

        NavigationUI.setupActionBarWithNavController(
            it, findNavController(), it.appBarConfiguration)
    }

}
