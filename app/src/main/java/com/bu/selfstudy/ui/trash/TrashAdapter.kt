package com.bu.selfstudy.ui.trash

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagedListAdapter
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.*
import com.bu.selfstudy.databinding.WordListItemBinding
import com.bu.selfstudy.databinding.BookListItemBinding
import com.bu.selfstudy.databinding.RecentWordListItemBinding
import com.bu.selfstudy.tool.log
import com.bu.selfstudy.tool.showToast
import java.lang.Exception

class TrashAdapter(val fragment: TrashFragment):RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val deleteRecordList = ArrayList<DeleteRecord>()
    var tracker: SelectionTracker<Long>? = null

    inner class BookViewHolder(val binding: BookListItemBinding) : RecyclerView.ViewHolder(binding.root)
    inner class WordViewHolder(val binding: RecentWordListItemBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = if(viewType == 0){
            BookViewHolder(BookListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
        }else{
            WordViewHolder(RecentWordListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
        }

        holder.itemView.setOnClickListener {
            "已點擊 ${deleteRecordList[holder.adapterPosition]}".showToast()
        }

        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        fragment.refreshDeleteRecord(deleteRecordList[position])
        when(holder){
            is BookViewHolder ->{
                with(holder.binding){
                    (deleteRecordList[position] as DeleteRecordBook).let {
                        bookNameTextView.text = it.bookName
                        bookSizeTextView.text = "${it.bookSize}個單字"
                        bookIcon.imageTintList = ColorStateList.valueOf(it.bookColorInt)
                    }
                }
            }
            is WordViewHolder->{
                with(holder.binding){
                    (deleteRecordList[position] as DeleteRecordWord).let {
                        wordNameTextView.text = it.wordName
                        bookNameTextView.text = it.bookName
                        divider.isVisible = false
                    }
                }
            }
        }
    }


    override fun getItemCount() = deleteRecordList.size

    override fun getItemViewType(position: Int) = when(deleteRecordList[position]){
        is DeleteRecordBook->0
        is DeleteRecordWord->1
        else -> throw Exception("don't use DeleteRecord, should use it's extend")
    }

    fun submitList(list: List<DeleteRecord>){
        deleteRecordList.clear()
        deleteRecordList.addAll(list)
        notifyDataSetChanged()
    }
}
