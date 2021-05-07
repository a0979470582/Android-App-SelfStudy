package com.bu.selfstudy.ui.word

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.WordSlideItemBinding
import com.bu.selfstudy.tool.log

/**
 * onBindViewHolder會觸發文字設定和決定是否擴展, 當viewHolder被重用, wordList被修改, 切換題庫等等會使它觸發,
 * 是否需要紀錄擴展狀態? 當內容由多(擴展)變成空(不應擴展)時, 擴展紀錄可能就不準確, 因此每一次onBindViewHolder
 * 參考依賴紀錄,重新決定是否擴展, 採用最低限度擴展
 */
class WordSlideAdapter(val wordList: ArrayList<Word> = ArrayList()) :
    RecyclerView.Adapter<WordSlideAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: WordSlideItemBinding) : RecyclerView.ViewHolder(binding.root)

    companion object InitialExpandState {

        private val stateMap = mutableMapOf<Long, List<Boolean>>()

        fun getState(word:Word): List<Boolean>{
            if(stateMap[word.id] == null)
                createState(word)

            return stateMap[word.id]!!
        }

        private fun createState(word:Word){
            val stateList = mutableListOf<Boolean>(false, false, false, false)
            //setting
            stateMap[word.id] = stateList
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WordSlideItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val realPosition: Int = position%wordList.size
        val word: Word = wordList[realPosition]
        //val stateList: List<Boolean>  = ExpandState.getState(word)


        holder.binding.word = word
        holder.binding.wordInfo.text = "${realPosition+1}/${wordList.size}"


        holder.binding.translationTextView.expand(false)
        holder.binding.variationTextView.expand(false)
        holder.binding.exampleTextView.collapse(false)
        holder.binding.noteTextView.expand(false)
        //holder.binding.wordInfo.text = "${wordList[realPosition].id}:${realPosition+1}/${wordList.size}"
    }

    override fun getItemCount() = when(wordList.size){
        0->0
        1->1
        else-> Int.MAX_VALUE
    }
    override fun getItemId(position: Int): Long = wordList[position].id

    fun setWordList(words: List<Word>){
        wordList.clear()
        wordList.addAll(words)
        notifyDataSetChanged()
    }

}