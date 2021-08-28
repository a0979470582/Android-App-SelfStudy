package com.bu.selfstudy.ui.main

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bu.selfstudy.R
import com.squareup.picasso.Picasso

object BindingAdapter {

    /**
     * 如果使用者沒有頭像, 就用預設大頭貼
     */
    @JvmStatic
    @BindingAdapter("android:src")
    fun setImageResource(imageView: ImageView, uri: Uri?){
        if(uri == null)
            imageView.setImageResource(R.drawable.ic_shooting_star)
        else{
            Picasso.get()
                    .load(uri)
                    .error(R.drawable.app_icon)
                    .into(imageView)
        }
    }

}