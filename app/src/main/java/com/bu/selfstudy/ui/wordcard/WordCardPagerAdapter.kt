package com.bu.selfstudy.ui.wordcard

import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.BuildConfig
import com.bu.selfstudy.R
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.WordCardItemBinding
import com.bu.selfstudy.tool.log

/**
 * onBindViewHolder會觸發文字設定和決定是否擴展, 當viewHolder被重用, wordList被修改, 切換題庫等等會使它觸發,
 * 是否需要紀錄擴展狀態? 當內容由多(擴展)變成空(不應擴展)時, 擴展紀錄可能就不準確, 因此每一次onBindViewHolder
 * 參考依賴紀錄,重新決定是否擴展, 採用最低限度擴展
 */
class WordCardPagerAdapter(
        val wordList: ArrayList<Word> = ArrayList()
) : RecyclerView.Adapter<WordCardPagerAdapter.ViewHolder>() {

    var mediaPlayer: MediaPlayer? = null

    fun mapToRealPosition(position: Int) = position%wordList.size
    fun submitList(words: List<Word>){
        wordList.clear()
        wordList.addAll(words)
        notifyDataSetChanged()
    }


    inner class ViewHolder(
            val binding: WordCardItemBinding
    ) : RecyclerView.ViewHolder(binding.root){

        fun resetExpandedState(){
            binding.translationTextView.expand(false)
            binding.variationTextView.expand(false)
            binding.exampleTextView.collapse(false)
            binding.noteTextView.expand(false)
        }
        fun bindData(word: Word){
            binding.word = word
            //binding.wordInfo.text = "${mapToRealPosition(adapterPosition)+1}/${wordList.size}"
            binding.wordInfo.text = "${mapToRealPosition(adapterPosition)+1}"

            SelfStudyApplication.context.resources.getResourceEntryName(R.raw.humor).log()
            SelfStudyApplication.context.resources.getResourceName(R.raw.humor).log()
            val uri = Uri.parse(word.audioPath)
            //SelfStudyApplication.context.resources.getResourceName(path)
            binding.soundButton.isVisible = false
        }
    }

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
        val binding = WordCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)

        holder.binding.soundButton.setOnClickListener {
            val uri = Uri.parse(
               "android.resource://${BuildConfig.APPLICATION_ID}/raw/${R.raw.particular}"
            )
            val mediaPlayer = MediaPlayer.create(SelfStudyApplication.context, uri)
            //wordList[realPosition].audioPath
            //SelfStudyApplication.context.resources.getIdentifier()
            mediaPlayer.start()
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        System.currentTimeMillis().log()

        val word: Word = wordList[mapToRealPosition(position)]
        holder.bindData(word)
        holder.resetExpandedState()

        //val stateList: List<Boolean>  = ExpandState.getState(word)

    }


    override fun getItemCount() = when(wordList.size){
        0->0
        1->1
        else-> Int.MAX_VALUE
    }
    override fun getItemId(position: Int): Long = wordList[mapToRealPosition(position)].id

}