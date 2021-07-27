package com.bu.selfstudy.ui.book

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.bu.selfstudy.ActivityViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogArchiveBook : AppCompatDialogFragment() {
    val args: DialogArchiveBookArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{
        return  MaterialAlertDialogBuilder(requireActivity())
                .setTitle(args.title)
                .setMessage(args.message)
                .setPositiveButton("確定") { dialog, which ->
                    setFragmentResult("DialogArchiveBook", Bundle())
                }
                .setNegativeButton("取消") { dialog, which ->
                }
                .create()
    }
}