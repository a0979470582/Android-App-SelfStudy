package com.bu.selfstudy.ui.word

import android.media.AudioManager
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.WordListItemBinding
import com.bu.selfstudy.databinding.RecyclerviewHeaderBinding
import com.bu.selfstudy.tool.log

/**
 * About SelectionTracker
 *
 * idList: Share Data
 * Fragment: Mediator
 * RecyclerView UI
 * RecyclerView Adapter
 * ItemDetails: [key, position]
 * ItemDetailsLookup: According to click point get RecyclerView.ItemViewHolder.adapterPosition,
 *                    map to ItemDetails = [idList[position], position]
 * ItemKeyProvider: from idList, key->position or position->key
 * EventBridge
 *
 * in Fragment:
 *      build SelectionTracker( IdItemKeyProvider, IdItemDetailsLookup ) then
 *      put in RecyclerView Adapter
 *
 * when touchEvent:
 *      ItemDetailsLookup output [key, position],
 *      -> add in selection
 *      -> EventBridge get position from ItemKeyProvider
 *      -> EventBridge run notifyItemChanged in adapter
 *
 * if Adapter has HeaderView(index=0), when touchEvent(index = 2):
 *      None      0
 *      idList[0] 1
 *      idList[1] 2
 *      idList[2] 3
 * Should revise ItemKeyProvider
 *
 */
class ListAdapter(val fragment: WordFragment):RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val asyncListDiffer = object: AsyncListDiffer<Word>(this, WordDiffCallback){}
    var tracker: SelectionTracker<Long>? = null

    inner class ItemViewHolder(val binding: WordListItemBinding) : RecyclerView.ViewHolder(binding.root)
    inner class HeaderViewHolder(val headerBinding: RecyclerviewHeaderBinding) : RecyclerView.ViewHolder(headerBinding.root)

    private val HEADER_VIEW_TYPE = 0
    private val ITEM_VIEW_TYPE = 1

    private var translationIsVisible = true

    private var isVeryFast = false

    private val mediaPlayer by lazy { MediaPlayer() }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == ITEM_VIEW_TYPE){
            val binding = WordListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            val holder = ItemViewHolder(binding)

            holder.itemView.setOnClickListener {
                fragment.switchRecyclerView(WordFragment.TYPE_CARD, holder.adapterPosition - 1)
            }

            holder.binding.markButton.setOnClickListener{
                asyncListDiffer.currentList[holder.adapterPosition]?.let { word->
                    fragment.updateMarkWord(word.id, !word.isMark)
                }
            }

            binding.soundButton.setOnClickListener {
                asyncListDiffer.currentList[holder.adapterPosition]?.let { word ->
                    mediaPlayer.apply {
                        reset()
                        setDataSource(word.audioFilePath)
                        setAudioStreamType(AudioManager.STREAM_MUSIC)
                        setOnPreparedListener{
                            it.start()
                        }
                        prepareAsync()
                    }
                }
            }

            return holder
        }else{
            val binding = RecyclerviewHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)

            return HeaderViewHolder(binding)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is ItemViewHolder -> {
                holder.binding.contentTextView.isVisible = translationIsVisible

                val word = asyncListDiffer.currentList[position]
                holder.binding.word = word
                holder.binding.markButton.setIconResource(
                    if (word.isMark)
                        R.drawable.ic_baseline_star_24
                    else
                        R.drawable.ic_round_star_border_24
                )
                tracker?.let {
                    holder.itemView.isActivated = it.isSelected(word.id)
                }
            }
            is HeaderViewHolder ->{
                holder.headerBinding.firstRow.text = "單字列表"
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty() && payloads[0] is Boolean) {
            val isMark = payloads[0] as Boolean
            (holder as ItemViewHolder).binding.markButton.setIconResource(
                if (isMark)
                    R.drawable.ic_baseline_star_24
                else
                    R.drawable.ic_round_star_border_24
            )
            return
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun getItemViewType(position: Int) =
        if(position == 0) HEADER_VIEW_TYPE else ITEM_VIEW_TYPE


    /**
     * 在一千筆數據中修改其中兩百筆資料, 其比對速度約在13ms,
     * 尤其預期使用者並不會在題庫中加入超過一千個單字, 因此可以使用DiffUtil
     */
    companion object WordDiffCallback : DiffUtil.ItemCallback<Word>(){
        override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem.isMark == newItem.isMark
        }

        override fun getChangePayload(oldItem: Word, newItem: Word): Any? {
            return if(oldItem.isMark == newItem.isMark)
                null
            else
                newItem.isMark
        }
    }

    override fun getItemCount() = asyncListDiffer.currentList.size


    fun submitList(words: List<Word>){
        if(words.isEmpty())
            asyncListDiffer.submitList(words)
        else
            asyncListDiffer.submitList(listOf(Word()).plus(words))
    }

    fun setTranslationIsVisible(isVisible: Boolean) {
        translationIsVisible = isVisible
    }

    fun setScrollingIsVeryFast(isVeryFast: Boolean){
        this.isVeryFast = isVeryFast
    }
}
