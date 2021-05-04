package com.bu.selfstudy.ui.word

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.databinding.WordItemBinding
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.tool.showToast

class WordAdapter(private val wordList: ArrayList<Word> = ArrayList()) :
        RecyclerView.Adapter<WordAdapter.ViewHolder>() {

    var tracker: SelectionTracker<Long>? = null

    inner class ViewHolder(val binding: WordItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)

        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            val word = wordList[position]
            "clicked word is ${word.wordName}".showToast()
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.word = wordList[position]

        tracker?.let {
            holder.itemView.isActivated = it.isSelected(wordList[position].id)
        }
    }

    override fun getItemCount() = wordList.size
    override fun getItemId(position: Int): Long = wordList[position].id

    fun setWordList(words: List<Word>){
        wordList.clear()
        wordList.addAll(words)
        notifyDataSetChanged()
    }
}
