package com.bu.selfstudy.ui.wordcard

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.BuildConfig
import com.bu.selfstudy.R
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.WordCardItem2Binding
import com.bu.selfstudy.databinding.WordCardItem3Binding
import com.bu.selfstudy.databinding.WordCardItemBinding
import com.bu.selfstudy.tool.log
import com.bu.selfstudy.tool.showToast
import java.io.IOException
import java.lang.IllegalArgumentException
import kotlin.math.max
import kotlin.math.min

/**
 * onBindViewHolder會觸發文字設定和決定是否擴展, 當viewHolder被重用, wordList被修改, 切換題庫等等會使它觸發,
 * 是否需要紀錄擴展狀態? 當內容由多(擴展)變成空(不應擴展)時, 擴展紀錄可能就不準確, 因此每一次onBindViewHolder
 * 參考依賴紀錄,重新決定是否擴展, 採用最低限度擴展
 */
class WordCardPagerAdapter2(
        val wordList: ArrayList<Word> = ArrayList()
) : RecyclerView.Adapter<WordCardPagerAdapter2.ViewHolder>(), LifecycleObserver{

    var mediaPlayer: MediaPlayer? = null

    inner class ViewHolder(
            val binding: WordCardItem2Binding
    ) : RecyclerView.ViewHolder(binding.root){

        fun bindData(word: Word){
            binding.word = word
            binding.soundButton.isVisible = true
            binding.pronunciationTextView.text = word.pronunciation.replace("KK[", "/")
                    .replace("]", "/")
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WordCardItem2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)


        holder.binding.soundButton.setOnClickListener {
            mediaPlayer?.run {
                if(duration != -1) {
                    start()
                    return@setOnClickListener
                }
            }
            "目前沒有音檔".showToast()
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word: Word = wordList[mapToRealPosition(position)]
        holder.bindData(word)
    }

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


    fun prepareMediaPlayer(fakePosition: Int){
        val url = wordList[mapToRealPosition(fakePosition)].audioPath
        try {
            mediaPlayer?.apply {
                reset()
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(url)
                prepareAsync()
            }
        }catch (e: IllegalArgumentException){
            e.printStackTrace()
        }catch (e: IOException){
            e.printStackTrace()
        }
    }

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

}