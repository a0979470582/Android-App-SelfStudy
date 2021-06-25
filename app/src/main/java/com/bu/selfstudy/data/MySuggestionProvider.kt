package com.bu.selfstudy.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.SearchRecentSuggestionsProvider
import android.database.Cursor
import android.net.Uri

class MySuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.bu.selfstudy.data.MySuggestionProvider"
        const val MODE: Int = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
    }
}