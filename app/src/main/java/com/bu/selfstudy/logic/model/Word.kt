package com.bu.selfstudy.logic.model

import android.net.Uri
import androidx.room.*
import java.util.*
@Entity(indices = [
        Index(value=["wordName"]),
        Index(value=["translation"]),
        Index(value=["variation"]),
        Index(value=["example"]),
        Index(value=["description"])
    ],
    foreignKeys = [
        ForeignKey(entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE)
    ]
)
data class Word(
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0,
    var bookId: Long,
    var wordName: String,
    var pronounceUri: String="",
    var translation: String="",
    var variation: String="",
    var example: String="",
    var description: String="",
    var dictionaryUri: String="",
    var timestamp: Date = Date(),
    var isTrash:Boolean = false
)
