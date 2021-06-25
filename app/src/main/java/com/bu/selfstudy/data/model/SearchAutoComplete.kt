package com.bu.selfstudy.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(indices = [Index(value=["searchName"], unique = true)])
data class SearchAutoComplete (
    @PrimaryKey(autoGenerate = true)
    val id:Long = 0,
    val searchName: String
)