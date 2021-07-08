package com.bu.selfstudy.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(indices = [Index(value=["searchName"], unique = true)])
data class SearchHistory (
        @PrimaryKey(autoGenerate = true)
        override var id:Long = 0,
        override var searchName: String,
        override var timestamp: Date = Date()
):SearchRow()