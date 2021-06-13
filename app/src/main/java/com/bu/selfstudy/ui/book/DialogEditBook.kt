package com.bu.selfstudy.ui.book

import android.app.ActionBar
import android.app.Dialog
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.marginStart
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.R
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.tool.putBundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class DialogEditBook() : AppCompatDialogFragment() {
    private val args: DialogEditBookArgs by navArgs()
    private lateinit var alertDialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{
        val linearLayout = requireActivity()
                .layoutInflater
                .inflate(R.layout.dialog_edit_book, null)

        val editText = linearLayout.findViewById<EditText>(R.id.editText).also {
            it.setText(args.bookName)
            it.addTextChangedListener {editable->
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = editable!!.isNotBlank()
            }
        }

        alertDialog = MaterialAlertDialogBuilder(requireActivity())
                .setView(linearLayout)
                .setPositiveButton("確定") { dialog, which ->
                    setFragmentResult(
                            "edit",
                            putBundle("bookName", editText.text.toString())
                    )
                }
                .setNegativeButton("取消") { dialog, which ->
                }
                .create()

        return alertDialog
    }
}