package com.bu.selfstudy.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*


@Entity
data class DeleteRecordWord(
    @PrimaryKey(autoGenerate = true)
    override var id: Long = 0,

    var wordId:Long,
    var wordName: String="",
    var bookId: Long,
    var bookName: String="",
    var theBookDeleted: Boolean=false,
    
    var timestamp: Date = Date()
):DeleteRecord()