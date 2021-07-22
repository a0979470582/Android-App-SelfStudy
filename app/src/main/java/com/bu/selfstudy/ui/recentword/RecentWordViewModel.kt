package com.bu.selfstudy.ui.recentword


import android.os.Bundle
import androidx.lifecycle.*
import androidx.paging.Config
import androidx.paging.LivePagedListBuilder
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.RecentWord
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.model.WordTuple
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.RecentWordRepository
import com.bu.selfstudy.data.repository.WordRepository
import com.bu.selfstudy.tool.SingleLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 更改資料庫中的wordList都會透過LiveData反應在目前的ViewPager, 例如新增,刪除,修改都會同步到資料庫,
 * 接著透過LiveData傳回並在ViewPager看到更新, 而wordFragment的生命週期進入onStop時, 會同步目前使用者所
 * 看到的單字頁(wordId)到資料庫
 */
class RecentWordViewModel() : ViewModel() {
    fun refreshRecentWord(recentWord: RecentWord) {
        viewModelScope.launch {
            RecentWordRepository.refreshRecentWord(recentWord)
        }
    }

    val recentWordLiveData = RecentWordRepository.loadRecentWord().asLiveData()

}