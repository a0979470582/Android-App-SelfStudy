package com.bu.selfstudy.ui.home
import androidx.navigation.fragment.findNavController

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.HomeListItemBinding

class HomeListAdapter(private val bookList: ArrayList<Book> = ArrayList()) :
        RecyclerView.Adapter<HomeListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: HomeListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HomeListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)

        holder.itemView.setOnClickListener {
            it.findNavController().navigate(R.id.wordFragment)
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.bookNameTextView.text = bookList[position].bookName
    }

    override fun getItemCount() = bookList.size
    override fun getItemId(position: Int): Long = bookList[position].id

    fun submitList(bookList: List<Book>){
        this.bookList.clear()
        this.bookList.addAll(bookList)
        notifyDataSetChanged()
    }
}
