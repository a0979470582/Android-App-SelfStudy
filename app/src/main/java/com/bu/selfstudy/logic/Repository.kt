package com.bu.selfstudy.logic

import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.logic.model.Book
import com.bu.selfstudy.logic.model.Member
import com.bu.selfstudy.logic.model.Word
import com.bu.selfstudy.logic.room.AppDatabase.Companion.getDatabase

object Repository {
    private val memberDao = getDatabase().memberDao()
    private val wordDao = getDatabase().wordDao()
    private val bookDao = getDatabase().bookDao()

    fun loadMember(memberId:Long=SelfStudyApplication.memberId) = memberDao.loadMember(memberId)
    fun loadBooks(memberId:Long=SelfStudyApplication.memberId) = bookDao.loadBooks(memberId)
    fun loadWords(bookId: Long, query: String) = wordDao.loadWordsDistinctUntilChanged(bookId, query)

    suspend fun insertWords(words: List<Word>) = wordDao.insertWords(*words.toTypedArray())
    suspend fun updateWords(words: List<Word>) = wordDao.updateWords(*words.toTypedArray())
    suspend fun deleteWords(words: List<Word>) = wordDao.deleteWords(*words.toTypedArray())

    suspend fun insertBook(book: Book) = bookDao.insertBook(book)
    suspend fun updateBook(book: Book) = bookDao.updateBook(book)
    suspend fun deleteBook(book: Book) = bookDao.deleteBook(book)

    suspend fun insertMember(member: Member) = memberDao.insertMember(member)
    suspend fun updateMember(member: Member) = memberDao.updateMember(member)
    suspend fun deleteMember(member: Member) = memberDao.deleteMember(member)
}