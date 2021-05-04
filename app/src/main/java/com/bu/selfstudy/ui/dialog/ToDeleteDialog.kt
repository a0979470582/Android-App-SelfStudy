package com.bu.selfstudy.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.navigation.fragment.navArgs
import com.bu.selfstudy.tool.setDialogResult
import com.bu.selfstudy.tool.setNavigationResult

class ToDeleteDialog() : AppCompatDialogFragment() {
    private val args: ToDeleteDialogArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{
        return  AlertDialog.Builder(requireActivity())
                .setTitle("刪除")
                .setMessage(args.message)
                .setPositiveButton("確定") { dialog, which ->
                    setNavigationResult("isDelete", true)
                }
                .setNegativeButton("取消") { dialog, which ->
                }
                .create()
    }
}