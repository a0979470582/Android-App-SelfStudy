package com.bu.selfstudy.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Book(
    @PrimaryKey(autoGenerate = true)
    var id:Long=0,
    var memberId: Long =0,
    var bookName: String = "我的第一本題庫",
    var timestamp: Date = Date()
)

