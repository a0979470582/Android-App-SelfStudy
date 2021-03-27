package com.bu.selfstudy.logic.model
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Member(
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0,
    var email: String,
    var password: String,
    var userName: String = "",
    var sex: String = "N",
    var iconUri: String = "content://",
    var timestamp: Date = Date(),
    var isTrash:Boolean = false
)
