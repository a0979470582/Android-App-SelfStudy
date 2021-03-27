package com.bu.selfstudy.logic.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(foreignKeys = [
    ForeignKey(entity = Member::class,
        parentColumns = ["id"],
        childColumns = ["memberId"],
        onDelete = ForeignKey.CASCADE
    )
])
data class Book(
    @PrimaryKey(autoGenerate = true)
    var id:Long=0,
    var memberId: Long,
    var bookName: String,
    var timestamp: Date = Date(),
    var isTrash:Boolean = false
)

