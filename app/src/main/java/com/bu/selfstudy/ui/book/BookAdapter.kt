package com.bu.selfstudy.ui.book

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.databinding.BookListItemBinding

class BookAdapter(val fragment: BookFragment):
        RecyclerView.Adapter<BookAdapter.ViewHolder>() {

    private val asyncListDiffer = object: AsyncListDiffer<Book>(this, BookDiffCallback){}

    var tracker: SelectionTracker<Long>? = null

    inner class ViewHolder(val binding: BookListItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BookListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)

        holder.itemView.setOnClickListener {
            val book = asyncListDiffer.currentList[holder.adapterPosition]
            fragment.navigateToWordCardFragment(book.id)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = asyncListDiffer.currentList[position]

        with(holder.binding){
            bookNameTextView.text = book.bookName
            bookSizeTextView.text = "${book.size}個單字"

            bookIcon.imageTintList = ColorStateList.valueOf(book.colorInt)
        }

        tracker?.let {
            holder.itemView.isActivated = it.isSelected(book.id)
        }
    }

    override fun getItemCount() = asyncListDiffer.currentList.size
    override fun getItemId(position: Int): Long = asyncListDiffer.currentList[position].id

    companion object BookDiffCallback : DiffUtil.ItemCallback<Book>(){
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }

    fun submitList(bookList: List<Book>){
        asyncListDiffer.submitList(bookList)
    }

}
