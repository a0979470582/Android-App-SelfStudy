package com.bu.selfstudy.ui.recentword

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.data.model.RecentWord
import com.bu.selfstudy.databinding.RecentWordListItemBinding

class RecentWordAdapter(val fragment: RecentWordFragment):
        RecyclerView.Adapter<RecentWordAdapter.ViewHolder>(){

    private val asyncListDiffer = object: AsyncListDiffer<RecentWord>(this, Diff_Callback){}

    inner class ViewHolder(val binding: RecentWordListItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bindData(recentWord: RecentWord){
            binding.wordNameTextView.text = recentWord.wordName
            binding.bookNameTextView.text = recentWord.bookName
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecentWordListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)

        val holder = ViewHolder(binding)

        holder.itemView.setOnClickListener {
            val recentWord = asyncListDiffer.currentList[holder.adapterPosition]
            fragment.navigateWordCardFragment(recentWord)

        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        asyncListDiffer.currentList[position].let { recentWord ->
            holder.bindData(recentWord)
            fragment.refreshRecentWord(recentWord)
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

    override fun getItemCount() = asyncListDiffer.currentList.size


    fun submitList(recentWordList: List<RecentWord>){
        asyncListDiffer.submitList(recentWordList)
    }

}
