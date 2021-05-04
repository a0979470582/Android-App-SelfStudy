package com.bu.selfstudy.ui.word

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.WordSlideItemBinding
import com.bu.selfstudy.tool.log

class WordSlideAdapter(val wordList: ArrayList<Word> = ArrayList()) :
    RecyclerView.Adapter<WordSlideAdapter.ViewHolder>() {

    var realPosition:Int = 0

    inner class ViewHolder(val binding: WordSlideItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WordSlideItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        realPosition = position%wordList.size
        holder.binding.word = wordList[realPosition]
        holder.binding.wordInfo.text = "${wordList[realPosition].id}:${realPosition+1}/${wordList.size}"
    }

    //override fun getItemCount() = wordList.size
    override fun getItemCount() = if(wordList.size == 0) 0 else Integer.MAX_VALUE

    override fun getItemId(position: Int): Long = wordList[position].id

    fun setWordList(words: List<Word>){
        wordList.clear()
        wordList.addAll(words)
        notifyDataSetChanged()
    }

}