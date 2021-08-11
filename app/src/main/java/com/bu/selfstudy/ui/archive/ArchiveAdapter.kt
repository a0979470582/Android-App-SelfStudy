package com.bu.selfstudy.ui.archive

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.databinding.BookListItemBinding
import com.bu.selfstudy.databinding.RecyclerviewHeaderBinding
import com.bu.selfstudy.ui.book.BookFragmentDirections
import com.google.android.material.button.MaterialButton


class ArchiveAdapter(val fragment: ArchiveFragment):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val asyncListDiffer = object: AsyncListDiffer<Book>(this, BookDiffCallback){}

    inner class ItemViewHolder(val binding: BookListItemBinding): RecyclerView.ViewHolder(binding.root)
    inner class HeaderViewHolder(val headerBinding: RecyclerviewHeaderBinding) : RecyclerView.ViewHolder(headerBinding.root)



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == 1){
            val binding = BookListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            val holder = ItemViewHolder(binding)


            holder.itemView.setOnClickListener {
                fragment.navigateToWordCardFragment(
                    asyncListDiffer.currentList[holder.adapterPosition].id)
            }

            holder.binding.bookIcon.setOnClickListener { v: View ->
                fragment.setChosenBook(asyncListDiffer.currentList[holder.adapterPosition])
                initPopupWindow(v)
            }

            holder.binding.moreIcon.setOnClickListener { v: View ->
                asyncListDiffer.currentList[holder.adapterPosition].let { book->
                    fragment.setChosenBook(book)
                    showMenu(v, R.menu.archive_action_mode, book)

                }

            }
            return holder
        }else{
            val binding = RecyclerviewHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)

            return HeaderViewHolder(binding)
        }
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int, book: Book) {
        val popup = PopupMenu(fragment.requireContext(), v)

        popup.menuInflater.inflate(menuRes, popup.menu)

        /**show Icon*/
        val method = popup.menu.javaClass.getDeclaredMethod("setOptionalIconsVisible", Boolean::class.javaPrimitiveType)
        method.isAccessible = true
        method.invoke(popup.menu, true)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.action_list -> {
                    findNavController(fragment).navigate(
                            ArchiveFragmentDirections.actionGlobalWordFragment(bookId = book.id))
                }
                R.id.action_edit -> {
                    findNavController(fragment).navigate(
                            ArchiveFragmentDirections.actionArchiveFragmentToEditBookDialog(book.bookName)
                    )
                }

                R.id.action_archive -> {
                    fragment.archiveBook(false)
                }

                R.id.action_delete -> {
                    findNavController(fragment).navigate(
                            ArchiveFragmentDirections.actionGlobalDialogDeleteCommon(
                                    "刪除題庫", "刪除「${book.bookName}」?")
                    )
                }


            }
            true
        }

        popup.show()
    }

    private fun initPopupWindow(v: View) {

        val linearLayout = LinearLayout(fragment.requireContext(), null, R.style.Theme_SelfStudy).also {
            it.layoutParams =  ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            )
            it.orientation = LinearLayout.HORIZONTAL
            it.setPadding(8,0,8,0)
        }

        val popupWindow = PopupWindow(linearLayout)


        fragment.requireContext().resources.getIntArray(R.array.book_color_list).forEach { colorInt->

            MaterialButton(
                    fragment.requireContext(),
                    null,
                    R.style.color_picker_IconOnly
            ).let { button ->
                button.icon = fragment.resources.getDrawable(R.drawable.ic_baseline_bookmark_24)
                button.iconTint = ColorStateList.valueOf(colorInt)
                button.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                button.isClickable = true
                button.iconPadding = 0
                button.setOnClickListener {
                    fragment.updateBookColor(button.iconTint.defaultColor)
                    popupWindow.dismiss()
                }
                linearLayout.addView(button)
            }

        }



        popupWindow.run {
            animationStyle = R.style.color_picker_animation
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            isFocusable = true//將焦點放在popupWindow, 彈出時可避免其他元件的點擊事件
            isOutsideTouchable = true
            elevation = 10f
            setBackgroundDrawable(ColorDrawable(Color.WHITE))//同時設置背景和高度才有陰影效果
            showAsDropDown(v)
        }


        val container = popupWindow.contentView.rootView
        val context = popupWindow.contentView.context
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        (container.layoutParams as WindowManager.LayoutParams).let { layoutParams ->
            layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            layoutParams.dimAmount = 0.3f
            windowManager.updateViewLayout(container, layoutParams)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is ItemViewHolder->{
                val book = asyncListDiffer.currentList[position]

                with(holder.binding){
                    bookNameTextView.text = book.bookName
                    bookSizeTextView.text = "${book.size}"

                    bookIcon.iconTint = ColorStateList.valueOf(book.colorInt)
                }
            }
            is HeaderViewHolder->{
                holder.headerBinding.firstRow.text = "封存"
            }
        }
    }

    override fun getItemCount() = asyncListDiffer.currentList.size
    override fun getItemId(position: Int): Long = asyncListDiffer.currentList[position].id
    override fun getItemViewType(position: Int) = if(position == 0) 0 else 1

    companion object BookDiffCallback : DiffUtil.ItemCallback<Book>(){
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }

    fun submitList(bookList: List<Book>){
        asyncListDiffer.submitList(listOf(Book()).plus(bookList))
    }

}
