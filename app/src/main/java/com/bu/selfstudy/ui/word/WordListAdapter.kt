package com.bu.selfstudy.ui.word

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.R
import com.bu.selfstudy.databinding.WordListItemBinding
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.tool.log
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller

class WordListAdapter(val fragment: WordFragment):
        RecyclerView.Adapter<WordListAdapter.ViewHolder>(),
        RecyclerViewFastScroller.OnPopupTextUpdate {

    private val asyncListDiffer = object: AsyncListDiffer<Word>(this, WordDiffCallback){}
    var tracker: SelectionTracker<Long>? = null

    inner class ViewHolder(val binding: WordListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WordListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)
        holder.itemView.setOnClickListener {
            val action = WordFragmentDirections.actionWordFragmentToWordCardFragment(holder.adapterPosition)
            it.findNavController().navigate(action)
        }

        holder.binding.markButton.setOnClickListener{
            val word = asyncListDiffer.currentList[holder.adapterPosition]
            if(word.isMark){
                fragment.cancelMarkWord(word)
            }else{
                fragment.markWord(word)
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = asyncListDiffer.currentList[position]

        holder.binding.word = word

        holder.binding.wordInfoTextView.text = (position+1).toString()
        holder.binding.markButton.setIconResource(
            if(word.isMark)
                R.drawable.ic_baseline_star_24
            else
                R.drawable.ic_round_star_border_24
        )


        holder.binding.divider.visibility =
                if(position == asyncListDiffer.currentList.size-1) View.GONE else View.VISIBLE

        tracker?.let {
            holder.itemView.isActivated = it.isSelected(word.id)
        }
    }

    override fun getItemCount() = asyncListDiffer.currentList.size
    override fun getItemId(position: Int): Long = asyncListDiffer.currentList[position].id

    companion object WordDiffCallback : DiffUtil.ItemCallback<Word>(){
        override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem == newItem
        }
    }

    fun submitList(wordList: List<Word>){
        asyncListDiffer.submitList(wordList)
    }

    override fun onChange(position: Int): CharSequence {
        return asyncListDiffer.currentList[position].wordName
    }

}
