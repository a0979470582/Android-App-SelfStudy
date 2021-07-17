package com.bu.selfstudy.data.model

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * 與其相關聯的Book和Word實體在使用者刪除它們時, 只會更動isTrash,
 * 因此紀錄id還算穩定
 */
@Entity(indices = [Index(value=["wordId", "bookId"], unique = true)])
data class RecentWord(
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0,
    var wordId: Long,
    var bookId: Long,
    var wordName: String = "",
    var bookName: String = "",
    var isExists:Boolean = true,
    var timestamp: Date = Date()
)