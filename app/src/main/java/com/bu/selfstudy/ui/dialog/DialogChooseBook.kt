package com.bu.selfstudy.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bu.selfstudy.ui.main.ActivityViewModel
import com.bu.selfstudy.R
import com.bu.selfstudy.SelfStudyApplication
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.databinding.ChooseBookListItemBinding
import com.bu.selfstudy.tool.putBundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogChooseBook : AppCompatDialogFragment() {
    private val args: DialogChooseBookArgs by navArgs()
    private val activityViewModel: ActivityViewModel by activityViewModels()

    private var selectedItem = 0
    private lateinit var listView: ListView

    inner class ViewHolder(val binding: ChooseBookListItemBinding) {

        var position = 0

        fun bindData(book: Book, position: Int){
            binding.bookNameTextView.text = book.bookName
            binding.bookSizeTextView.text = book.size.toString()
            binding.explanationTextView.text = book.explanation
            binding.bookIcon.imageTintList = ColorStateList.valueOf(book.colorInt)
            this.position = position

            binding.radioButton.isChecked = this.position == selectedItem

        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val bookList = ArrayList<Book>(activityViewModel.bookListLiveData.value!!)

        var defaultPosition = activityViewModel.bookIdList.indexOf(args.bookId)
        if(defaultPosition == -1)
            defaultPosition = 0

        selectedItem = defaultPosition


        val adapter = object: BaseAdapter(){
            override fun getCount() = bookList.size
            override fun getItem(position: Int) = bookList[position]
            override fun getItemId(position: Int) = bookList[position].id
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                if(convertView == null){

                    val binding=  ChooseBookListItemBinding.inflate(
                            LayoutInflater.from(requireActivity()), parent, false
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

        listView = ListView(requireActivity()).also {

            it.setPadding(0, 32,0,0)
            it.adapter = adapter
            it.post {
                it.setSelection(selectedItem)
            }
        }

        /**接收變更通知是為了加入題庫時能即時反映*/
        lifecycleScope.launchWhenStarted {
            activityViewModel.bookListLiveData.observe(this@DialogChooseBook){
                bookList.clear()
                bookList.addAll(it)
                adapter.notifyDataSetChanged()
            }
        }


        val alertDialog =  MaterialAlertDialogBuilder(requireActivity())
            .setTitle(args.title)
            .setView(listView)
            .setPositiveButton("確認"){ _, _ ->
                setFragmentResult("DialogChooseBook", putBundle("bookId", bookList[selectedItem].id))
                findNavController().popBackStack()
            }
            .setNegativeButton("取消"){ _, _ ->
            }
            .setNeutralButton("新增題庫"){_, _ ->
                findNavController().navigate(R.id.addBookFragment)
            }
            .create()


        alertDialog.show()

        activityViewModel.bookListLiveData.observe(this){
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = it.isNotEmpty()
        }

        return alertDialog
    }
}