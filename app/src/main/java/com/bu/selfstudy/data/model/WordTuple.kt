package com.bu.selfstudy.data.model

import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class WordTuple(
        var id:Long,
        var bookId: Long,
        var wordName: String,
        var pronunciation: String,
        var audioPath:String,
        var isMark: Boolean,
        var timestamp: Date,
        var isTrash:Boolean
): Parcelable
