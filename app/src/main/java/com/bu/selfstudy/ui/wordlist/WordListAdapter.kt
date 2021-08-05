package com.bu.selfstudy.ui.wordlist

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.WordListItemBinding
import com.bu.selfstudy.data.model.WordTuple
import com.bu.selfstudy.databinding.RecyclerviewHeaderBinding
import com.bu.selfstudy.tool.log
import com.bu.selfstudy.tool.showToast
import com.bu.selfstudy.ui.book.BookAdapter

/**
 * About SelectionTracker
 *
 * idList: Share Data
 * Fragment: Mediator
 * RecyclerView UI
 * RecyclerView Adapter
 * ItemDetails: [key, position]
 * ItemDetailsLookup: According to click point get RecyclerView.ViewHolder.adapterPosition,
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
class WordListAdapter(val listFragment: WordListFragment):RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val asyncListDiffer = object: AsyncListDiffer<Word>(this, WordDiffCallback){}
    var tracker: SelectionTracker<Long>? = null

    inner class ItemViewHolder(val binding: WordListItemBinding) : RecyclerView.ViewHolder(binding.root)
    inner class HeaderViewHolder(val headerBinding: RecyclerviewHeaderBinding) : RecyclerView.ViewHolder(headerBinding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == 1){
            val binding = WordListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            val holder = ItemViewHolder(binding)

            holder.itemView.setOnClickListener {
                val word = asyncListDiffer.currentList[holder.adapterPosition]
                if(word != null)
                    "已點擊 ${word.wordName}".showToast()
            }

            holder.binding.markButton.setOnClickListener{
                val word = asyncListDiffer.currentList[holder.adapterPosition]
                if(word != null)
                    listFragment.updateMarkWord(word.id, !word.isMark)
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
                val word = asyncListDiffer.currentList[position]
                if (word != null) {
                    holder.binding.wordNameTextView.text = word.wordName
                    holder.binding.pronunciationTextView.text = word.pronunciation
                    holder.binding.markButton.setIconResource(
                            if (word.isMark)
                                R.drawable.ic_baseline_star_24
                            else
                                R.drawable.ic_round_star_border_24
                    )
                    tracker?.let {
                        holder.itemView.isActivated = it.isSelected(word.id)
                    }
                } else {
                    holder.binding.wordNameTextView.text = ""
                    holder.binding.pronunciationTextView.text = ""
                    holder.binding.markButton.setIconResource(R.drawable.ic_round_star_border_24
                    )
                }
                //holder.binding.wordInfoTextView.text = (position+1).toString()
                holder.binding.divider.visibility =
                        if (position == asyncListDiffer.currentList.size - 1) View.GONE else View.VISIBLE

            }
            is HeaderViewHolder ->{
                holder.headerBinding.firstRow.text = "單字列表"
            }
        }
    }

    override fun getItemViewType(position: Int) = if(position == 0) 0 else 1


    /**
     * 在一千筆數據中修改其中兩百筆資料, 其比對速度約在13ms,
     * 尤其預期使用者並不會在題庫中加入超過一千個單字, 因此可以使用DiffUtil
     */
    companion object WordDiffCallback : DiffUtil.ItemCallback<Word>(){
        override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem.isMark == newItem.isMark &&
                    oldItem.pronunciation == newItem.pronunciation
                    oldItem.wordName == newItem.wordName
        }
    }

    override fun getItemCount() = asyncListDiffer.currentList.size

    fun submitList(words: List<Word>){
        asyncListDiffer.submitList(listOf(Word()).plus(words))
    }
}
