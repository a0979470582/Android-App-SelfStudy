package com.bu.selfstudy.ui.dialog

import android.app.Dialog
import android.app.usage.UsageEvents
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bu.selfstudy.tools.setDialogResult
import com.bu.selfstudy.tools.setNavigationResult

class ChooseBookDialog : AppCompatDialogFragment() {
    private val args: ChooseBookDialogArgs by navArgs()
    private var position = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
                .setTitle(args.message)
                .setSingleChoiceItems(args.bookNames, position){_, which->
                    position = which
                }
                .setPositiveButton("確認"){ _, _ ->
                    setDialogResult(
                            "action" to args.action ,
                            "bookId" to args.bookIds[position]
                    )
                }
                .setNegativeButton("取消"){ _, _ ->
                    setDialogResult(isSuccessful = false)
                }
                .create()
        }
}