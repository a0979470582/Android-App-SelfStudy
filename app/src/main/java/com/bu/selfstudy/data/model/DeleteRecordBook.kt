package com.bu.selfstudy.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class DeleteRecordBook(
        @PrimaryKey(autoGenerate = true)
        override var id: Long = 0,

        var bookId: Long,
        var bookName: String="",
        var bookSize: Int=0,
        var bookColorInt:Int=0,


        var timestamp: Date = Date()
):DeleteRecord()