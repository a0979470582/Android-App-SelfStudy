package com.bu.selfstudy.ui.recentword

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.RecentWord
import com.bu.selfstudy.databinding.WordListItemBinding
import com.bu.selfstudy.data.model.WordTuple
import com.bu.selfstudy.databinding.RecentWordListItemBinding
import com.bu.selfstudy.tool.showToast
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller

class RecentWordAdapter(val fragment: RecentWordFragment):
        PagedListAdapter<RecentWord, RecentWordAdapter.ViewHolder>(Diff_Callback){

    private val asyncListDiffer = object: AsyncListDiffer<RecentWord>(this, Diff_Callback){}

    inner class ViewHolder(val binding: RecentWordListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecentWordListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)

        val holder = ViewHolder(binding)

        holder.itemView.setOnClickListener {
            val recentWord = getItem(holder.adapterPosition)

            recentWord?.let {
                fragment.navigateWordCardFragment(it)
            }

        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = getItem(position)
        if(word != null){

        }else{

        }
    }

    companion object Diff_Callback : DiffUtil.ItemCallback<RecentWord>(){
        override fun areItemsTheSame(oldItem: RecentWord, newItem: RecentWord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecentWord, newItem: RecentWord): Boolean {
            return oldItem == newItem
        }
    }
}
