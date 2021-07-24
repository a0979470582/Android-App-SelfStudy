package com.bu.selfstudy.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


open class DeleteRecord (
        @Ignore
        open val id:Long=0
)