package com.bu.selfstudy.ui.search

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.*
import com.bu.selfstudy.databinding.WordListItemBinding
import com.bu.selfstudy.databinding.BookListItemBinding
import com.bu.selfstudy.databinding.SearchSuggestionListItemBinding
import com.bu.selfstudy.tool.log
import com.bu.selfstudy.tool.showToast
import com.bu.selfstudy.ui.book.BookFragment
import kotlinx.coroutines.*
import kotlin.random.Random

class SuggestionListAdapter(val fragment: SearchFragment):
        RecyclerView.Adapter<SuggestionListAdapter.ViewHolder>() {

    private val suggestionList = ArrayList<SearchRow>()

    inner class ViewHolder(val binding: SearchSuggestionListItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SearchSuggestionListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)

        holder.itemView.setOnClickListener {
            val suggestionName = suggestionList[holder.adapterPosition].searchName
            fragment.startSearch(suggestionName)
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.suggestionNameTextView.text = suggestionList[position].searchName
        with(holder.binding){ when(suggestionList[position]){
                is SearchHistory->{
                    historyIcon.visibility = View.VISIBLE
                    searchIcon.visibility = View.GONE
                }
                is SearchAutoComplete->{
                    historyIcon.visibility = View.GONE
                    searchIcon.visibility = View.VISIBLE
                }
        } }
    }

    override fun getItemCount() = suggestionList.size


    suspend fun submitList(_suggestionList: List<SearchRow>) = coroutineScope {
        withContext(Dispatchers.Default){
            suggestionList.clear()
            suggestionList.addAll(_suggestionList)
        }
        notifyDataSetChanged()
    }
}
