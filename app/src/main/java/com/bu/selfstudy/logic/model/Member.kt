package com.bu.selfstudy.logic.model
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Member(
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0,
    var email: String = "default",
    var password: String = "default",
    var userName: String = "default",
    var sex: Int = 2,//012:男女無
    var iconUri: String ?= "content://...",//TODO
    var timestamp: Date = Date(),
    var isTrash:Boolean = false

)
