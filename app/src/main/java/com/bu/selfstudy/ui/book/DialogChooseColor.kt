package com.bu.selfstudy.ui.book

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import com.bu.selfstudy.R
import com.bu.selfstudy.tool.putBundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogChooseColor() : AppCompatDialogFragment()  {

    private lateinit var alertDialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val linearLayout = requireActivity().layoutInflater.inflate(
                R.layout.book_color_list_item, null
        )


        alertDialog = MaterialAlertDialogBuilder(requireActivity())
                .setView(linearLayout)
                .create()


        return alertDialog
    }
}