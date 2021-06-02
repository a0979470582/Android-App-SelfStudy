package com.bu.selfstudy.ui.book

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.tool.setNavigationResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EditBookDialog() : AppCompatDialogFragment() {
    private val args: EditBookDialogArgs by navArgs()
    private val editText = EditText(SelfStudyApplication.context).also {
        it.setPadding(16, 0 , 0 ,16)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{
        editText.setText(args.bookName)


        return  MaterialAlertDialogBuilder(requireActivity())
                .setTitle("修改題庫")
                .setMessage("輸入題庫名稱")
                .setView(editText)
                .setPositiveButton("確定") { dialog, which ->
                    val inputBookName = editText.text.toString()
                    inputBookName?.let {
                        if(it.isNotBlank()){
                            setFragmentResult("edit", Bundle().also { bundle ->
                                bundle.putString("bookName", it)
                            })
                        }
                    }
                }
                .setNegativeButton("取消") { dialog, which ->
                }
                .create()
    }
}