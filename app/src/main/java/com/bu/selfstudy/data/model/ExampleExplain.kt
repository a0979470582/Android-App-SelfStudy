package com.bu.selfstudy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
class ExampleExplain (
        @PrimaryKey(autoGenerate = true)
        var id:Long = 0,
        var partOfSpeechId: Long=0,
        var explain: String="",
        var timestamp: Date = Date()
)