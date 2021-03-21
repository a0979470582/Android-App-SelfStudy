package com.bu.selfstudy.logic.model

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Word(
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0,
    var bookId: Long = 0,
    @ColumnInfo(name="name", typeAffinity = ColumnInfo.TEXT)
    var wordName: String,
    var pronounceUri: String ?= null,
    var translation: String ?= null,
    var variation: String ?= null,
    var example: String ?= null,
    var description: String ?= null,
    var dictionaryUri: String ?= null,
    var timestamp: Date = Date(),
    var isTrash:Boolean = false
)
