package com.bu.selfstudy.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(indices = [Index(value=["searchName"], unique = true)])
class SearchHistory (
        @PrimaryKey(autoGenerate = true)
    val id:Long = 0,
    searchName: String,
    val timestamp: Date = Date()
):SearchRow(searchName)