package com.bu.selfstudy.data.model

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Book和Word實體在使用者刪除時, 只會更動isTrash,
 * 因此紀錄id還算穩定
 *
 * 此RecentWord是歷史單字紀錄, 會先記錄當下資料, 未來即便Book改名,
 * 也會在onBindViewHolder中要求更新一次資訊
 *
 */
@Entity(indices = [Index(value=["wordId"], unique = true)])
data class RecentWord(
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0,
    var wordId: Long = 0,
    var bookId: Long = 0,
    var wordName: String = "",
    var bookName: String = "",
    var isTrash: Boolean = false,//目前是否存在於回收桶(未來可能回復)
    var timestamp: Date = Date()
)