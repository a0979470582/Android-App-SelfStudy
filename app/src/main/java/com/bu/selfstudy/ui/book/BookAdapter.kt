package com.bu.selfstudy.ui.book

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.R
import com.bu.selfstudy.databinding.BookItemBinding
import com.bu.selfstudy.logic.model.Book
import com.bu.selfstudy.logic.model.Word
import java.text.DateFormat

class BookAdapter(private val bookList:ArrayList<Book> =  ArrayList<Book>()) : RecyclerView.Adapter<BookAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: BookItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BookItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)

        holder.itemView.setOnClickListener {
            val book = bookList[holder.adapterPosition]
            val action = BookFragmentDirections.actionBookFragmentToWordFragment(book.id, book.bookName)
            Navigation.findNavController(binding.root).navigate(action)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = bookList[position]
        holder.binding.bookIdTxV.text = "bookid: ${book.id}"
        holder.binding.memberIdTxV.text = "memberid: ${book.memberId}"
        holder.binding.bookNameTxV.text = book.bookName
        holder.binding.timestampTxV.text =
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            .format(book.timestamp)
            .toString()
    }

    override fun getItemCount() = bookList.size

    fun setBookList(books: List<Book>){
        bookList.clear()
        bookList.addAll(books)
    }
}
