package com.bu.selfstudy.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bu.selfstudy.SelfStudyApplication.Companion.context
import com.bu.selfstudy.data.dao.*
import com.bu.selfstudy.data.model.*
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.SearchRepository
import com.bu.selfstudy.tool.log
import kotlinx.coroutines.*
import java.util.concurrent.Executors

@Database(version = 1, entities = [
    Member::class,
    Book::class,
    Word::class,
    SearchHistory::class,
    SearchAutoComplete::class,
    RecentWord::class
])
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun bookDao(): BookDao
    abstract fun wordDao(): WordDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun searchAutoCompleteDao(): SearchAutoCompleteDao
    abstract fun recentWordDao():RecentWordDao

    companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(): AppDatabase {
            instance?.let {
                return it
            }
            return  Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                    .createFromAsset("database/app_database")
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Executors.newSingleThreadExecutor().execute {
                                //instance?.initialize()
                            }
                        }
                    })
                    .build().apply {
                        instance = this
                }

        }
    }


    private fun initialize(){
        val member = Member(
                email = "a0979470582@gmail.com",
                password = "123456789",
                userName = "LuLu",
                sex = "F",
                iconPath = "icon.jpg"
        )
        val job = Job()
        val scope = CoroutineScope(job)
        scope.launch{
            Thread.currentThread().name.log()
            member.id = memberDao().insert(member)[0]
            SearchRepository.insertLocalAutoComplete("bookData/no_repeat_word.txt")
        }
    }
}