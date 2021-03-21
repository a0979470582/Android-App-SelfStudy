package com.bu.selfstudy.ui.word

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.databinding.WordItemBinding
import com.bu.selfstudy.logic.model.Word
import com.bu.selfstudy.showToast
import java.text.DateFormat

class WordAdapter(private val wordList: ArrayList<Word> = ArrayList<Word>()) : RecyclerView.Adapter<WordAdapter.ViewHolder>() {
    var tracker: SelectionTracker<Long>? = null

    inner class ViewHolder(val binding: WordItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun getItemDetails() = IdItemDetails(getItemId(adapterPosition), adapterPosition)
    }

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
        val word = wordList[position]
        holder.binding.apply {
            numberTxV.text = "$position"
            wordNameTxV.text = word.wordName
            pronounceTxV.text = "KK[ˋsæŋktʃʊ͵ɛrɪ]  DJ[ˋsæŋktjuəri]  美式 "
            translationTxV.text = word.translation
        }
        tracker?.let {
            holder.itemView.isActivated = it.isSelected(wordList[position].id)
        }
    }

    override fun getItemCount() = wordList.size
    override fun getItemId(position: Int): Long = wordList[position].id
    internal fun getPosition(key: Long): Int = wordList.indexOfFirst {
        it.id == key
    }
    fun setWordList(words: List<Word>){
        wordList.clear()
        wordList.addAll(words)
    }
}
