package com.bu.selfstudy.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.tool.putBundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogChooseBook : AppCompatDialogFragment() {
    private val args: DialogChooseBookArgs by navArgs()
    private val activityViewModel: ActivityViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val bookList = activityViewModel.bookListLiveData.value!!

        val bookNameList = if(bookList.isEmpty()) arrayOf("新題庫1, 新題庫2")
                           else bookList.map { it.bookName }.toTypedArray()


        var defaultPosition = activityViewModel.bookIdList.indexOf(args.bookId)
        if(defaultPosition == -1)
            defaultPosition = 0

        var position = defaultPosition

        return MaterialAlertDialogBuilder(requireActivity())
            .setTitle(args.title)
            .setSingleChoiceItems(bookNameList, defaultPosition){ _, which->
                position = which
            }
            .setPositiveButton("確認"){ _, _ ->
                setFragmentResult("DialogChooseBook", putBundle("bookId", bookList[position].id))
                findNavController().popBackStack()
            }
            .setNegativeButton("取消"){ _, _ ->
            }
            .create()
    }
}