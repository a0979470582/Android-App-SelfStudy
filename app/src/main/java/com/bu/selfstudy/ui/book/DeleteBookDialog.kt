package com.bu.selfstudy.ui.book

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.tool.setNavigationResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DeleteBookDialog() : AppCompatDialogFragment() {
    private val args: DeleteBookDialogArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{
        return  MaterialAlertDialogBuilder(requireActivity())
                .setTitle("刪除題庫")
                .setMessage("刪除 「${args.bookName}」?")
                .setPositiveButton("確定") { dialog, which ->
                    setFragmentResult("delete", Bundle())
                }
                .setNegativeButton("取消") { dialog, which ->
                }
                .create()
    }
}