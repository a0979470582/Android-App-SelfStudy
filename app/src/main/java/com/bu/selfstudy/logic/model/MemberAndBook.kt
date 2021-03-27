package com.bu.selfstudy.logic.model

import androidx.room.Embedded
import androidx.room.Relation

data class MemberAndBook(
    @Embedded val member: Member,
    @Relation(
        parentColumn = "id",
        entityColumn = "memberId"
    )
    val books: List<Book>
)
