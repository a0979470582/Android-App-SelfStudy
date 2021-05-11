package com.bu.selfstudy.ui.editword

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.repository.WordRepository

class EditWordViewModel(wordId: Long) : ViewModel()  {

    val wordLiveData = WordRepository.loadWord(wordId).asLiveData()


    var editWord: Word? = null


    val hasEditLiveData = MutableLiveData(false)

    fun setHasEdit(){
        wordLiveData.value?.let { word->
            hasEditLiveData.value = !compareWord(word, editWord!!)
        }
    }

    private fun compareWord(word1:Word, word2:Word): Boolean{
        return (
            word1.id == word2.id &&
            word1.wordName == word2.wordName &&
            word1.pronunciation == word2.pronunciation &&
            word1.translation == word2.translation &&
            word1.variation == word2.variation &&
            word1.example == word2.example &&
            word1.note == word2.note
        )
    }

    companion object {
        fun provideFactory(wordId: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return EditWordViewModel(wordId) as T
            }
        }
    }
}