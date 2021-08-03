package com.bu.selfstudy.data.model

import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class WordTuple(
        var id:Long=0,
        var bookId: Long=0,
        var wordName: String="",
        var pronunciation: String="",
        var audioFilePath:String="",
        var isMark: Boolean=false,
        var timestamp: Date?=null,
        var isTrash:Boolean=false
): Parcelable
