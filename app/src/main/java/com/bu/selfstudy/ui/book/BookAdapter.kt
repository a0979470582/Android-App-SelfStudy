package com.bu.selfstudy.ui.book

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.databinding.BookItemBinding
import com.bu.selfstudy.logic.model.Book
import com.bu.selfstudy.ui.word.WordActivity
import java.text.DateFormat

class BookAdapter(private val fragment: BookFragment, private val bookList: List<Book>) : RecyclerView.Adapter<BookAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: BookItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BookItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            val book = bookList[position]
            val intent = Intent(parent.context, WordActivity::class.java).apply {
                putExtra("book_id", book.id)
                putExtra("book_name", book.bookName)
            }
            fragment.startActivity(intent)
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
}
