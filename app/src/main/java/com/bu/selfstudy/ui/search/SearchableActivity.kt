package com.bu.selfstudy.ui.search

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.data.MySuggestionProvider
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.databinding.ActivitySearchBinding
import com.bu.selfstudy.databinding.FragmentBookBinding
import com.bu.selfstudy.tool.showToast
import com.bu.selfstudy.tool.viewBinding

class SearchableActivity : AppCompatActivity(){
    private val binding : ActivitySearchBinding by viewBinding()
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.wordLiveData.observe(this){
            updateView(it)
        }

        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                startSearch(query)
            }
        }
    }

    private fun updateView(result: Result<Word>) {
        val word = result.getOrNull()
        if(word != null){
            binding.wordNameTextView.text = word.wordName
            binding.exampleTextView.setContentText(word.example)
            binding.pronunciationTextView.text = word.pronunciation
            binding.translationTextView.setContentText(word.translation)
            binding.variationTextView.setContentText(word.variation)
        }else{
            "字典中沒有此字".showToast()
        }
    }

    private fun startSearch(query: String){
        viewModel.getWordPage(query)
    }
}