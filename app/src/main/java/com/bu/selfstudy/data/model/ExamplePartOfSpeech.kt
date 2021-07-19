package com.bu.selfstudy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
class ExamplePartOfSpeech (
        @PrimaryKey(autoGenerate = true)
        var id:Long = 0,
        var wordId: Long=0,
        var english: String="",// vt./ vi./ n.
        var chinese: String="",// 及物動詞/ 不及物動詞/ 名詞
        var timestamp: Date = Date()
)