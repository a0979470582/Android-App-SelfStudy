package com.bu.selfstudy.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(foreignKeys = [
    ForeignKey(entity = Member::class,
        parentColumns = ["id"],
        childColumns = ["memberId"],
        onDelete = ForeignKey.CASCADE
    )
])
@Parcelize
data class Book(
    @PrimaryKey(autoGenerate = true)
    var id:Long=0,
    var memberId: Long,
    var bookName: String,
    var timestamp: Date = Date(),
    var currentWordId:Long= 0,
    var sortState:String = "id_asc",
    var photoPosition:Int = 0,
    var isTrash:Boolean = false
):Parcelable

