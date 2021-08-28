package com.bu.selfstudy.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogDeleteCommon : AppCompatDialogFragment() {
    private val args: DialogDeleteCommonArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{
        return  MaterialAlertDialogBuilder(requireActivity())
                .setTitle(args.title)
                .setMessage(args.message)
                .setPositiveButton("確定") { dialog, which ->
                    setFragmentResult("DialogDeleteCommon", Bundle())
                }
                .setNegativeButton("取消") { dialog, which ->
                }
                .create()
    }
}