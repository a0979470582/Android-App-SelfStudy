package com.bu.selfstudy.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

open class SearchRow (
        @Ignore
        open var id: Long=0,
        @Ignore
        open var searchName: String="",
        @Ignore
        open var timestamp: Date = Date()
)