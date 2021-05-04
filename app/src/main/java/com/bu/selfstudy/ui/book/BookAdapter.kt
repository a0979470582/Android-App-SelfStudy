package com.bu.selfstudy.ui.book

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.databinding.BookItemBinding
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.databinding.WordItemBinding
import com.bu.selfstudy.tool.showToast

class BookAdapter(private val bookFragment:BookFragment, private var bookList:List<Book> =  ArrayList()) :
        RecyclerView.Adapter<BookAdapter.ViewHolder>() {

    var tracker: SelectionTracker<Long>? = null

    inner class ViewHolder(val binding: BookItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val binding = BookItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
        )
        val holder = ViewHolder(binding)


        //BookFragment To WordFragment
        holder.itemView.setOnClickListener {
            val book = bookList[holder.adapterPosition]
            bookFragment.navigateToWordFragment(book)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.book = bookList[position]
        tracker?.let {
            holder.itemView.isActivated = it.isSelected(bookList[position].id)
        }
    }

    override fun getItemCount() = bookList.size
    override fun getItemId(position: Int): Long = bookList[position].id

}
