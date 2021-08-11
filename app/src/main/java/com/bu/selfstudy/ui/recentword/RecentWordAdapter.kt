package com.bu.selfstudy.ui.recentword

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.data.model.RecentWord
import com.bu.selfstudy.databinding.RecentWordListItemBinding
import com.bu.selfstudy.databinding.RecyclerviewHeaderBinding
import com.bu.selfstudy.databinding.WordListItemBinding
import com.bu.selfstudy.ui.mark.MarkAdapter

class RecentWordAdapter(val fragment: RecentWordFragment):
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val asyncListDiffer = object: AsyncListDiffer<RecentWord>(this, Diff_Callback){}


    private val HEADER_VIEW_TYPE = 0
    private val ITEM_VIEW_TYPE = 1

    inner class HeaderViewHolder(val headerBinding: RecyclerviewHeaderBinding) : RecyclerView.ViewHolder(headerBinding.root)
    inner class ItemViewHolder(val binding: RecentWordListItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bindData(recentWord: RecentWord){
            binding.wordNameTextView.text = recentWord.wordName
            binding.bookNameTextView.text = recentWord.bookName
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == ITEM_VIEW_TYPE) {

            val binding = RecentWordListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)

            val holder = ItemViewHolder(binding)

            holder.itemView.setOnClickListener {
                val recentWord = asyncListDiffer.currentList[holder.adapterPosition]
                fragment.navigateWordCardFragment(recentWord)

            }

            return holder
        }else{
            val binding = RecyclerviewHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)

            return HeaderViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is ItemViewHolder -> {
                asyncListDiffer.currentList[position].let { recentWord ->
                    holder.bindData(recentWord)
                    fragment.refreshRecentWord(recentWord)
                }
            }
            is HeaderViewHolder ->{
                holder.headerBinding.firstRow.text = "最近單字"
            }
        }

    }


    override fun getItemViewType(position: Int) =
            if(position == 0) HEADER_VIEW_TYPE else ITEM_VIEW_TYPE


    companion object Diff_Callback : DiffUtil.ItemCallback<RecentWord>(){
        override fun areItemsTheSame(oldItem: RecentWord, newItem: RecentWord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecentWord, newItem: RecentWord): Boolean {
            return oldItem == newItem
        }
    }

    override fun getItemCount() = asyncListDiffer.currentList.size


    fun submitList(recentWordList: List<RecentWord>){
        if(recentWordList.isEmpty())
            asyncListDiffer.submitList(recentWordList)
        else
            asyncListDiffer.submitList(listOf(RecentWord()).plus(recentWordList))
    }

}
