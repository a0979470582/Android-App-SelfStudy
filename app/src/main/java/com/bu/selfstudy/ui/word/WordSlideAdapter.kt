package com.bu.selfstudy.ui.word

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.WordSlideItemBinding

class WordSlideAdapter(private val wordList: ArrayList<Word> = ArrayList()) :
    RecyclerView.Adapter<WordSlideAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: WordSlideItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WordSlideItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.word = wordList[position]
        holder.binding.wordInfo.text = "${wordList[position].id}:$position/${wordList.size}"
    }

    override fun getItemCount() = wordList.size
    override fun getItemId(position: Int): Long = wordList[position].id

    fun setWordList(words: List<Word>){
        wordList.clear()
        wordList.addAll(words)
        notifyDataSetChanged()
    }

}