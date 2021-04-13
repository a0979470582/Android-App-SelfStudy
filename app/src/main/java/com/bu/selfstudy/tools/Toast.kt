package com.bu.selfstudy.tools

import android.widget.Toast
import com.bu.selfstudy.SelfStudyApplication

fun <T:Any> T.showToast(duration:Int = Toast.LENGTH_SHORT){
    Toast.makeText(SelfStudyApplication.context, this.toString(), duration).show()
}