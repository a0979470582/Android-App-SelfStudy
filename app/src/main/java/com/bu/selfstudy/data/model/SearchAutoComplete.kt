package com.bu.selfstudy.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(indices = [Index(value=["searchName"], unique = true)])
data class SearchAutoComplete (
        @PrimaryKey(autoGenerate = true)
        override var id:Long = 0,
        override var searchName: String,
        var isHistory: Boolean = false
):SearchRow()