package com.bu.selfstudy.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.tool.setDialogResult
import com.bu.selfstudy.tool.setNavigationResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ToDeleteDialog() : AppCompatDialogFragment() {
    private val args: ToDeleteDialogArgs by navArgs()
    private val activityViewModel: ActivityViewModel by activityViewModels()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{
        return  MaterialAlertDialogBuilder(requireActivity())
                .setTitle("刪除")
                .setMessage(args.message)
                .setPositiveButton("確定") { dialog, which ->
                    activityViewModel.deleteWordToTrash(args.wordId)
                    //setNavigationResult("isDelete", true)
                }
                .setNegativeButton("取消") { dialog, which ->
                }
                .create()
    }
}