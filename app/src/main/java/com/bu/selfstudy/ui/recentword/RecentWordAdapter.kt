package com.bu.selfstudy.ui.recentword

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.data.model.RecentWord
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.RecentWordListItemBinding
import com.bu.selfstudy.databinding.RecyclerviewHeaderBinding
import com.bu.selfstudy.databinding.WordListItemBinding
import com.bu.selfstudy.ui.mark.MarkAdapter

class RecentWordAdapter(val fragment: RecentWordFragment):
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val asyncListDiffer = object: AsyncListDiffer<RecentWord>(this, Diff_Callback){}


    private val HEADER_VIEW_HOLDER = 0
    private val ITEM_VIEW_HOLDER = 1

    inner class HeaderViewHolder(val headerBinding: RecyclerviewHeaderBinding) : RecyclerView.ViewHolder(headerBinding.root)
    inner class ItemViewHolder(val binding: RecentWordListItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bindData(recentWord: RecentWord){
            binding.wordNameTextView.text = recentWord.wordName
            binding.bookNameTextView.text = recentWord.bookName
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == ITEM_VIEW_HOLDER) {

            val binding = RecentWordListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)

            val holder = ItemViewHolder(binding)

            binding.root.setOnClickListener {
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


    //沒數據時會顯示HeaderView
    override fun getItemCount() = asyncListDiffer.currentList.size
    override fun getItemViewType(position: Int) =
        if(position == 0) HEADER_VIEW_HOLDER else ITEM_VIEW_HOLDER


    companion object Diff_Callback : DiffUtil.ItemCallback<RecentWord>(){
        override fun areItemsTheSame(oldItem: RecentWord, newItem: RecentWord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecentWord, newItem: RecentWord): Boolean {
            return oldItem.bookId == newItem.bookId &&
                    oldItem.bookName == newItem.bookName &&
                    oldItem.wordId == newItem.wordId &&
                    oldItem.wordName == newItem.wordName
        }
    }


    fun submitList(wordList: List<RecentWord>){
        asyncListDiffer.submitList(listOf(RecentWord()).plus(wordList))
    }

}
