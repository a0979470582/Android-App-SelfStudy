package com.bu.selfstudy.ui.book

import android.os.Bundle
import androidx.lifecycle.*
import com.bu.selfstudy.R
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.data.repository.WordRepository
import com.bu.selfstudy.tool.SingleLiveData
import com.bu.selfstudy.tool.log
import com.bu.selfstudy.tool.putBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BookViewModel : ViewModel(){


    val databaseEvent = SingleLiveData<Pair<String, Bundle?>>()

    val bookNameMap = mapOf(
            "toeicData.json" to "多益高頻單字",
            "ieltsData.json" to "雅思核心單字",
            "commonly_use_1000.json" to "最常用1000字",
            "commonly_use_3000.json" to "最常用3000字",
            "high_school_level_1.json" to "高中英文分級Level1",
            "high_school_level_2.json" to "高中英文分級Level2",
            "high_school_level_3.json" to "高中英文分級Level3",
            "high_school_level_4.json" to "高中英文分級Level4",
            "high_school_level_5.json" to "高中英文分級Level5",
            "high_school_level_6.json" to "高中英文分級Level6",
            "junior_school_basic_1200.json" to "國中基礎英文1200字",
            "junior_school_difficult_800.json" to "國中進階英文800字",
            "elementary_school_basic_word.json" to "小學基礎單字"
    )

    var longPressedBook: Book? = null
    fun refreshLongPressedBook(bookList: List<Book>, bookId: Long){
        viewModelScope.launch {
            longPressedBook = bookList.firstOrNull{ it.id==bookId }
        }
    }

    fun insertLocalBook(bookName: String){
        viewModelScope.launch {
            BookRepository.insertLocalBook(bookName).let {
                databaseEvent.postValue("insertLocal" to putBundle("bookName", "bookName"))
            }
        }
    }

    fun deleteBook(bookId: Long, bookName: String){
        viewModelScope.launch {
            if(BookRepository.deleteBookToTrash(bookId) > 0)
                databaseEvent.postValue("delete" to putBundle("bookName", bookName))
        }
    }
    fun insertBook(bookName: String){
        viewModelScope.launch {
            val book = Book(bookName = bookName, memberId = SelfStudyApplication.memberId)
            if(BookRepository.insertBook(book).isNotEmpty()) {
                databaseEvent.postValue("insertBook" to null)
            }
        }
    }

    fun updateBook(book: Book){
        viewModelScope.launch{
            if(BookRepository.updateBook(book)>0) {
                databaseEvent.postValue("update" to null)
            }
        }
    }

}