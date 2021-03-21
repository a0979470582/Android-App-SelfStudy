package com.bu.selfstudy.ui.word

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner


class WordViewModelFactory(
        private val bookId: Long,
        private val bookName: String,
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle?
): AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        return WordViewModel(handle, bookId, bookName) as T
    }
}
