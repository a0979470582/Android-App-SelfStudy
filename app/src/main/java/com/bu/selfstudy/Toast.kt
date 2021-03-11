package com.bu.selfstudy

import android.widget.Toast

fun String.showToast(duration:Int = Toast.LENGTH_SHORT){
    Toast.makeText(SelfStudyApplication.context, this, duration).show()
}
fun Int.showToast(duration:Int = Toast.LENGTH_SHORT){
    Toast.makeText(SelfStudyApplication.context, this, duration).show()
}