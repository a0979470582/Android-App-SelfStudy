package com.bu.selfstudy.ui.wordcard

import android.content.res.ColorStateList
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.BuildConfig
import com.bu.selfstudy.R
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.WordCardItemBinding
import com.bu.selfstudy.tool.hasNetwork
import com.bu.selfstudy.tool.log
import com.bu.selfstudy.tool.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.lang.IllegalArgumentException
import java.nio.file.Path
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

/**
 *
 */
class WordCardPagerAdapter(
        private val fragment: WordCardFragment,
        private val wordList: ArrayList<Word> = ArrayList()
) : RecyclerView.Adapter<WordCardPagerAdapter.ViewHolder>(), LifecycleObserver{

    var mediaPlayer: MediaPlayer? = null

    inner class ViewHolder(
            val binding: WordCardItemBinding
    ) : RecyclerView.ViewHolder(binding.root){

        fun resetUi() {
            resetExpandedState()
            binding.scrollView.scrollTo(0, 0)
        }

        fun resetExpandedState(){
            binding.translationTextView.expand(false)
            binding.variationTextView.expand(false)
            binding.exampleTextView.expand(false)
            binding.synonymsTextView.expand((false))
            binding.noteTextView.expand(false)
        }

        fun bindData(word: Word){
            binding.word = word
            //binding.wordInfo.text = "${mapToRealPosition(adapterPosition)+1}/${wordList.size}"
            binding.wordInfo.text = "${mapToRealPosition(adapterPosition)+1}"


            binding.soundButton.isVisible = word.audioFilePath.isNotEmpty()
            binding.translationTextView.isVisible = word.translation.isNotEmpty()
            binding.variationTextView.isVisible = word.variation.isNotEmpty()
            binding.exampleTextView.isVisible = word.example.isNotEmpty()
            binding.synonymsTextView.isVisible = word.synonyms.isNotEmpty()
            binding.noteTextView.isVisible = word.note.isNotEmpty()
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WordCardItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
        )
        val holder = ViewHolder(binding)


        holder.binding.soundButton.setOnClickListener {
            val word = wordList[mapToRealPosition(holder.adapterPosition)]
            prepareMediaPlayer(word.audioFilePath)
        }


        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word: Word = wordList[mapToRealPosition(position)]
        holder.resetUi()
        holder.bindData(word)

        //checkAudioPath(word.id, word.audioFilePath)
    }

    private fun checkAudioPath(wordId:Long, path:String){
        fragment.lifecycleScope.launch(Dispatchers.IO) {
            if(path.isBlank() || !File(SelfStudyApplication.context
                            .filesDir.absolutePath+"/"+path).exists()){
                fragment.downloadAudio(wordId)
            }
        }
    }
    private fun prepareMediaPlayer(audioFilePath: String) {
        try {
            mediaPlayer?.apply {
                reset()
                setDataSource(SelfStudyApplication.context
                        .filesDir.absolutePath+"/"+audioFilePath)
                setOnPreparedListener{
                    it.start()
                }
                prepareAsync()
            }
        }catch (e: IllegalArgumentException){
            e.printStackTrace()
            "目前沒有音檔".showToast()
        }catch (e: IOException){
            e.printStackTrace()
            if(hasNetwork())
                "此單字無音檔".showToast()
            else
                "目前沒有網路連接，請稍後再試".showToast()
        }
    }



    /**
     * 採用無盡滾動的設計, 因此這adapter內的position都是fake
     */
    fun mapToFakePosition(position: Int) = if(wordList.size >= 2)
             wordList.size * 100 + min(position, wordList.lastIndex)
        else
            max(position, 0)

    fun mapToRealPosition(position: Int) = position%wordList.size

    fun submitList(words: List<Word>){
        wordList.clear()
        wordList.addAll(words)
        notifyDataSetChanged()
    }

    override fun getItemCount() = when(wordList.size){
        0->0
        1->1
        else-> Int.MAX_VALUE
    }
    override fun getItemId(position: Int): Long = wordList[mapToRealPosition(position)].id

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun createMediaPlayer(){
        if(mediaPlayer == null){
            mediaPlayer = MediaPlayer()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun releaseMediaPlayer(){
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun notifyRemoveOneWord(realPosition: Int){
        wordList.removeAt(realPosition)
        notifyItemRemoved(realPosition)
    }

}