package com.bu.selfstudy.ui.book

import com.bu.selfstudy.R
import com.bu.selfstudy.SelfStudyApplication
import com.leinardi.android.speeddial.SpeedDialActionItem

object ActionItemCreator {
    private val _resources = SelfStudyApplication.context.resources

    val addBookItem = SpeedDialActionItem
            .Builder(R.id.book_fragment_fab_add_book, R.drawable.ic_baseline_menu_book_24)
            .setLabel(_resources.getString(R.string.FAB_add_book))
            .create()

    val addWordItem = SpeedDialActionItem
            .Builder(R.id.book_fragment_fab_add_word, R.drawable.ic_baseline_add_word_24)
            .setLabel(_resources.getString(R.string.FAB_add_word))
            .create()
}