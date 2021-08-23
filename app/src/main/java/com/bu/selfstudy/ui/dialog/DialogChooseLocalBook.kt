package com.bu.selfstudy.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.database.DataSetObserver
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.bu.selfstudy.ActivityViewModel
import com.bu.selfstudy.R
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.data.model.LocalBook
import com.bu.selfstudy.data.repository.BookRepository
import com.bu.selfstudy.databinding.LocalBookListItemBinding
import com.bu.selfstudy.tool.log
import com.bu.selfstudy.tool.putBundle
import com.bu.selfstudy.ui.book.BookAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView

class DialogChooseLocalBook : AppCompatDialogFragment() {
    private var selectedItem = 0

    inner class ViewHolder(val binding: LocalBookListItemBinding) {

        var position = 0

        fun bindData(book: LocalBook, position: Int){
            binding.bookNameTextView.text = book.bookName
            binding.bookSizeTextView.text = book.size
            binding.explanationTextView.text = book.explanation
            binding.fileSizeTextView.text = book.fileSize

            this.position = position

            binding.radioButton.isChecked = this.position == selectedItem

        }
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val bookList = ArrayList<LocalBook>()

        val adapter = object: BaseAdapter(){
            override fun getCount() = bookList.size
            override fun getItem(position: Int) = bookList[position]
            override fun getItemId(position: Int) = position.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                if(convertView == null){
                    val binding=  LocalBookListItemBinding.inflate(
                            LayoutInflater.from(SelfStudyApplication.context), parent, false
                    )
                    val holder = ViewHolder(binding).also {
                        it.bindData(bookList[position], position)
                    }

                    val view = holder.binding.root.also {
                        it.tag = holder
                        it.setOnClickListener {
                            selectedItem = holder.position
                            notifyDataSetChanged()
                        }
                    }

                    return view
                }else{
                    (convertView.tag as ViewHolder).bindData(bookList[position], position)

                    return convertView
                }
            }
        }

        val listView = ListView(SelfStudyApplication.context).also {

            it.setPadding(0, 32,0,0)
            it.adapter = adapter
        }

        lifecycleScope.launchWhenStarted {
            val result = BookRepository.loadLocalBookNames()
            bookList.clear()
            bookList.addAll(result)
            adapter.notifyDataSetChanged()
        }

        return MaterialAlertDialogBuilder(requireActivity())
            .setTitle("選擇要匯入的題庫")
            .setView(listView)
            .setPositiveButton("確認"){ _, _ ->
                setFragmentResult(
                        "DialogChooseLocalBook",
                        putBundle("bookName", bookList[selectedItem].bookName)
                        .putBundle("explanation", bookList[selectedItem].explanation)
                )
                findNavController().popBackStack()
            }
            .setNegativeButton("取消"){ _, _ ->
            }
            .create()
    }
}