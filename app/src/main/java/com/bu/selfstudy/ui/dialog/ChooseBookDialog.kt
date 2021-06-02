package com.bu.selfstudy.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.tool.setDialogResult
import com.bu.selfstudy.tool.setNavigationResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ChooseBookDialog : AppCompatDialogFragment() {
    private val activityViewModel: ActivityViewModel by activityViewModels()

    private var position = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bookList = activityViewModel.bookListLiveData.value
        val defaultPosition = bookList!!.indexOf(activityViewModel.currentOpenBookLiveData.value)

        val bookNames= bookList?.map { it.bookName }?.toTypedArray()

        return MaterialAlertDialogBuilder(requireActivity())
            .setTitle("選擇加入的題庫")
            .setSingleChoiceItems(bookNames, defaultPosition){_, which->
                position = which
            }
            .setPositiveButton("確認"){ _, _ ->
                setNavigationResult("bookId", bookList[position].id)
                findNavController().popBackStack()
            }
            .setNegativeButton("取消"){ _, _ ->
            }
            .create()
    }
}