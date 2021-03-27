package com.bu.selfstudy.logic.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 可查詢多筆Book對應List<Word>的數據, 但在應用中, 我們只查一筆Book中的Word,
 因此這兩類暫時用不到
 */
data class BookAndWord(
    @Embedded val book: Book,
    @Relation(
        parentColumn = "id",
        entityColumn = "bookId"
    )
    val words:List<Word>
)
