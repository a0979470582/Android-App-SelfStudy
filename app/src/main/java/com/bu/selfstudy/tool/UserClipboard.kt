package com.bu.selfstudy.tool

import android.app.Activity
import android.app.Application
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.view.MenuItem
import com.bu.selfstudy.R

fun Activity.getClipboardText(): String?{
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    if(!clipboard.hasPrimaryClip())
        return null

    if(!clipboard.primaryClipDescription!!.hasMimeType(MIMETYPE_TEXT_PLAIN))
        return null

    val text = clipboard.primaryClip!!.getItemAt(0).text


    if(text.isNullOrBlank())
        return null

    return text.toString()
}