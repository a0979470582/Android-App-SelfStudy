package com.bu.selfstudy.ui.editword

import android.os.Bundle
import androidx.lifecycle.*
import com.bu.selfstudy.data.model.Word
import com.bu.selfstudy.data.repository.WordRepository
import com.bu.selfstudy.tool.SingleLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditWordViewModel(val word: Word) : ViewModel()  {

    val databaseEvent = SingleLiveData<Pair<String, Bundle?>>()

    val wordLiveData = WordRepository.loadOneWord(word.id).asLiveData()

    val hasEditLiveData = MutableLiveData(false)

    fun updateWord(){
        viewModelScope.launch(Dispatchers.IO) {
            if(WordRepository.updateWord(word)>0)
                databaseEvent.postValue("update" to null)
        }
    }


    fun setEditState(){
        wordLiveData.value?.let {
            hasEditLiveData.value = (word!=it)
        }
    }

    companion object {
        fun provideFactory(word: Word): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return EditWordViewModel(word) as T
            }
        }
    }
}