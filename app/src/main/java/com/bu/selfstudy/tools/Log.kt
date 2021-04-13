package com.bu.selfstudy.tools

import android.util.Log

inline fun <reified T:Any> T.log(){
    Log.e("debug", this.toString())
}