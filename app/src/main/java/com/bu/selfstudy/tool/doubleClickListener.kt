package com.bu.selfstudy.tool

import android.view.View
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import com.bu.selfstudy.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun View.setOnDoubleClickListener(block: (View)->Unit){

}

interface OnDoubleClickListener: View.OnClickListener{
    override fun onClick(v: View?) {
        var clickedOnce: Boolean = false

        if (clickedOnce) {
            //do double click event
            return
        }

        clickedOnce = true
        val job = Job()
        val scope = CoroutineScope(job)
        scope.launch {
            delay(800)
            clickedOnce = false
        }
    }
}