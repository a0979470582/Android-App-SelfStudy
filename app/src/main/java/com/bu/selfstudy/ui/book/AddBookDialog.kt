package com.bu.selfstudy.ui.book

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddBookDialog() : AppCompatDialogFragment() {
    private val editText = EditText(SelfStudyApplication.context).also {
        it.setPadding(16, 0 , 0 ,16)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{
        return  MaterialAlertDialogBuilder(requireActivity())
                .setTitle("新增題庫")
                .setMessage("輸入題庫名稱")
                .setView(editText)
                .setPositiveButton("確定") { dialog, which ->
                    val inputBookName = editText.text.toString()
                    inputBookName?.let {
                        if(it.isNotBlank()){
                            setFragmentResult("insert", Bundle().also { bundle->
                                bundle.putString("bookName", inputBookName!!)
                            })
                        }
                    }
                }
                .setNegativeButton("取消") { dialog, which ->
                }
                .create()
    }
}