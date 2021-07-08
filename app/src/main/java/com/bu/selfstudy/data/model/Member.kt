package com.bu.selfstudy.data.model
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

//登入流程必須從此處開始處理
@Entity
data class Member(
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0,
    var email: String,
    var password: String,
    var userName: String = "",
    var sex:String = "N",

    var position: Int = 0,
    var backgroundImageIndex:Int = 0,
    var iconPath: String = "",

    var timestamp: Date = Date(),
    var isTrash:Boolean = false
)
