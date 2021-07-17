package com.bu.selfstudy.data.repository

import androidx.lifecycle.liveData
import androidx.paging.Config
import androidx.paging.LivePagedListBuilder
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Member
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.AppDatabase.Companion.getDatabase
import com.bu.selfstudy.data.model.RecentWord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RecentWordRepository {
    private val recentWordDao = getDatabase().recentWordDao()

    fun loadRecentWord() = liveData<List<RecentWord>> {
        val a = recentWordDao.loadRecentWord()

    }

    suspend fun insertRecentWord(recentWord: RecentWord) = withContext(Dispatchers.IO){
        recentWordDao.insertRecentWord(recentWord)
    }

}