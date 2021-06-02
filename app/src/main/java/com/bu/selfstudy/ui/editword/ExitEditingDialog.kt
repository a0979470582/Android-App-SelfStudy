package com.bu.selfstudy.ui.editword

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.R
import com.bu.selfstudy.tool.setDialogResult
import com.bu.selfstudy.tool.setNavigationResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ExitEditingDialog() : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{
        return  MaterialAlertDialogBuilder(requireActivity())
                .setTitle("離開編輯")
                .setMessage("是否保存修改?")
                .setPositiveButton("保存") { dialog, which ->
                    setNavigationResult("exitAndSave", true)
                }
                .setNegativeButton("不保存") { dialog, which ->
                    findNavController().popBackStack(R.id.wordFragment, false)
                }
                .setNeutralButton("取消"){dialog, which ->
                    dialog.dismiss()
                }
                .create()
    }
}