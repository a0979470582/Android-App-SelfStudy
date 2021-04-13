package com.bu.selfstudy.data.model
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity
@Parcelize
data class Member(
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0,
    var email: String,
    var password: String,
    var userName: String = "",
    var iconUri: String = "content://",
    var sex:String = "N",
    var timestamp: Date = Date(),
    var currentBookId: Long = 0,
    var isTrash:Boolean = false
):Parcelable
