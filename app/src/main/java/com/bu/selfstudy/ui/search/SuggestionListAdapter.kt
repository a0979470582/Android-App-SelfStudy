package com.bu.selfstudy.ui.search

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.R
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.*
import com.bu.selfstudy.databinding.SearchSuggestionListItemBinding
import com.bu.selfstudy.tool.log
import kotlinx.coroutines.*
import java.lang.StrictMath.min


class SuggestionListAdapter(val fragment: SearchFragment):
        RecyclerView.Adapter<SuggestionListAdapter.ViewHolder>() {

    private val suggestionList = ArrayList<SearchRow>()
    private val searchQueryColor = SelfStudyApplication.context.resources
                                    .getColor(R.color.searchQuerySpan)
    private var searchQuery = ""
    private var deleteItemPosition = -1

    inner class ViewHolder(
            val binding: SearchSuggestionListItemBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun showHistoryIcon(){
            binding.historyIcon.isVisible = true
            binding.closeIcon.isVisible = true
            binding.searchIcon.isVisible = false
        }
        fun showSearchIcon(){
            binding.historyIcon.isVisible = false
            binding.closeIcon.isVisible = false
            binding.searchIcon.isVisible = true
        }
    }

    override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val holder = ViewHolder(SearchSuggestionListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))

        holder.binding.root.setOnClickListener {
            val suggestionName = suggestionList[holder.adapterPosition].searchName
            fragment.startSearch(suggestionName)
        }

        holder.binding.closeIcon.setOnClickListener {
            fragment.removeSearchHistory(
                    suggestionList[holder.adapterPosition] as SearchHistory
            )
            deleteItemPosition = holder.adapterPosition
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //填入每一列的文字
        val span = SpannableString(suggestionList[position].searchName).also {
            it.setSpan(ForegroundColorSpan(searchQueryColor), 0, min(searchQuery.length, it.length),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        when(suggestionList[position]){
            is SearchHistory->{
                holder.showHistoryIcon()
                holder.binding.suggestionNameTextView.text = span
            }
            is SearchAutoComplete->{
                holder.showSearchIcon()
                holder.binding.suggestionNameTextView.text = span
            }
            is SearchRow->{
                holder.showSearchIcon()
                val clipboardText = suggestionList[position].searchName
                setClipboardTextOnFirstRow(holder.binding.suggestionNameTextView,
                        3, "您複製的文字「${clipboardText}」")
            }
        }
    }

    //限制使用者的剪貼簿中的文字長度
    private fun setClipboardTextOnFirstRow(textView: TextView, maxLine: Int, showText: String){
        textView.text = showText
        if(textView.lineCount > maxLine) {
            val endPosition = textView.layout.getLineEnd(maxLine - 1)
            val limitText = showText.subSequence(0, endPosition - 4).toString() + "...」"
            textView.text = limitText
        }
    }
    //此List中的第一行可能是或不是使用者的剪貼簿文字
    override fun getItemCount() = suggestionList.size


    suspend fun submitList(_suggestionList: List<SearchRow>, ) = coroutineScope {
        withContext(Dispatchers.Default){
            suggestionList.clear()
            suggestionList.addAll(_suggestionList)
        }
        if(deleteItemPosition != -1) {
            notifyItemRemoved(deleteItemPosition)
            deleteItemPosition = -1
        }
        else
            notifyDataSetChanged()
    }

    fun refreshSearchQuery(searchQuery: String) {
        this.searchQuery = searchQuery
    }
}
