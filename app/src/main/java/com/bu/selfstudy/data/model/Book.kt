package com.bu.selfstudy.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.bu.selfstudy.SelfStudyApplication
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(foreignKeys = [
    ForeignKey(entity = Member::class,
        parentColumns = ["id"],
        childColumns = ["memberId"],
        onDelete = ForeignKey.CASCADE
    )
])
data class Book(
        @PrimaryKey(autoGenerate = true)
        var id:Long = 0,
        var memberId: Long = SelfStudyApplication.memberId,
        var bookName: String="",
        var size: Int = 0,

        var position: Int = 0,
        var sortState:String = "time_asc",//time, letter, proficiency
        var displayMode:Int = 0,//card, list, simple
        var colorInt: Int = 0,
        var explanation: String="",

        var timestamp: Date = Date(),
        var isArchive: Boolean = false
)

