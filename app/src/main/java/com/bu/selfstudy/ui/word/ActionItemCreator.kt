package com.bu.selfstudy.ui.word

import com.bu.selfstudy.R
import com.bu.selfstudy.SelfStudyApplication
import com.leinardi.android.speeddial.SpeedDialActionItem

object ActionItemCreator {
    private val _resources = SelfStudyApplication.context.resources

    val markItem = SpeedDialActionItem
            .Builder(R.id.fab_mark_word, R.drawable.ic_round_star_border_24)
            .setLabel(_resources.getString(R.string.FAB_mark))
            .create()

    val cancelMarkItem = SpeedDialActionItem
            .Builder(R.id.fab_cancel_mark_word, R.drawable.ic_baseline_star_24)
            .setLabel(_resources.getString(R.string.FAB_cancel_mark))
            .create()

    val addItem = SpeedDialActionItem
            .Builder(R.id.fab_add_word, R.drawable.ic_baseline_search_24)
            .setLabel(_resources.getString(R.string.FAB_add))
            .create()

    val editItem = SpeedDialActionItem
            .Builder(R.id.fab_edit_word, R.drawable.ic_round_edit_24)
            .setLabel(_resources.getString(R.string.FAB_edit))
            .create()

    val deleteItem = SpeedDialActionItem
            .Builder(R.id.fab_delete_word, R.drawable.ic_round_delete_24)
            .setLabel(_resources.getString(R.string.FAB_delete))
            .create()
}