package com.bu.selfstudy.ui.word

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.databinding.WordItemBinding
import com.bu.selfstudy.logic.model.Word
import com.bu.selfstudy.showToast
import java.text.DateFormat

class WordAdapter(private val wordList: List<Word>) : RecyclerView.Adapter<WordAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: WordItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            val word = wordList[position]
            val intent = Intent(parent.context, WordActivity::class.java).apply {
                putExtra("word_id", word.id)
                putExtra("word_name", word.wordName)
            }
            "clicked word is ${word.wordName}".showToast()
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = wordList[position]
        holder.binding.numberTxV.text = "$position"
        holder.binding.wordNameTxV.text = word.wordName
        holder.binding.pronounceTxV.text = "KK[ˋsæŋktʃʊ͵ɛrɪ]  DJ[ˋsæŋktjuəri]  美式 "
        holder.binding.translationTxV.text = word.translation
    }

    override fun getItemCount() = wordList.size
}
