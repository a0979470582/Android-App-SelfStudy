package com.bu.selfstudy.ui.editword

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.bu.selfstudy.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogSaveAndExit() : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{
        return  MaterialAlertDialogBuilder(requireActivity())
                .setTitle("離開編輯")
                .setMessage("是否保存修改?")
                .setPositiveButton("保存") { dialog, which ->
                    setFragmentResult("saveAndExit", Bundle())
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