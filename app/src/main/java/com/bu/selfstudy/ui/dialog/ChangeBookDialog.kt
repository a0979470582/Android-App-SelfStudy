package com.bu.selfstudy.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.tool.setDialogResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ChangeBookDialog : AppCompatDialogFragment() {
    private val activityViewModel: ActivityViewModel by activityViewModels()

    private var position = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val bookList = activityViewModel.bookList
        val defaultPosition = activityViewModel.position

        val bookNames= bookList.map { it.bookName }.toTypedArray()

        return MaterialAlertDialogBuilder(requireActivity())
                .setTitle("切換至題庫")
                .setSingleChoiceItems(bookNames, defaultPosition){_, which->
                    position = which
                }
                .setPositiveButton("確認"){ _, _ ->
                    if(position == defaultPosition)
                        dialog!!.dismiss()
                    activityViewModel.updateInitialBookId(bookList[position].id)
                }
                .setNegativeButton("取消"){ _, _ ->
                }
                .create()
        }
}