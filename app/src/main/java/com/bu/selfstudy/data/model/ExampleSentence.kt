package com.bu.selfstudy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity
class ExampleSentence (
        @PrimaryKey(autoGenerate = true)
        var id:Long = 0,
        var explainId: Long=0,
        var sentenceEnglish: String="",
        var sentenceChinese: String="",
        var markText: String="",// "do,did,does"
        var timestamp: Date = Date()
)