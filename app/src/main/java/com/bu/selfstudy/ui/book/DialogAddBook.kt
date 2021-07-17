package com.bu.selfstudy.ui.book

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.R
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.tool.openKeyboard
import com.bu.selfstudy.tool.putBundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogAddBook() : AppCompatDialogFragment() {
    private lateinit var alertDialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{
        val linearLayout = requireActivity().layoutInflater.inflate(
                R.layout.dialog_add_book, null
        )

        val editText = linearLayout.findViewById<EditText>(R.id.editText).also {
            it.addTextChangedListener {editable->
                alertDialog.getButton(
                        AlertDialog.BUTTON_POSITIVE
                ).isEnabled = editable!!.isNotBlank()
            }
            it.requestFocus()
        }

        alertDialog = MaterialAlertDialogBuilder(requireActivity())
                .setView(linearLayout)
                .setPositiveButton("確定") { dialog, which ->
                    setFragmentResult("DialogAddBook",
                            putBundle("bookName", editText.text.toString())
                    )
                }
                .setNegativeButton("取消") { dialog, which ->
                }
                .create()

        alertDialog.run {
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            show()
            getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false//禁用確定鈕
        }

        return alertDialog
    }

}