package com.bu.selfstudy.ui.book

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
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bu.selfstudy.R
import com.bu.selfstudy.data.model.Book
import com.bu.selfstudy.databinding.BookListItemBinding
import com.google.android.material.button.MaterialButton


class BookAdapter(val fragment: BookFragment):
        RecyclerView.Adapter<BookAdapter.ViewHolder>() {

    private val asyncListDiffer = object: AsyncListDiffer<Book>(this, BookDiffCallback){}

    var tracker: SelectionTracker<Long>? = null

    inner class ViewHolder(val binding: BookListItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BookListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)


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
                showMenu(v, R.menu.book_action_mode, book)

            }

        }
        return holder
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
                            BookFragmentDirections.actionBookFragmentToWordListFragment(bookId = book.id))
                }
                R.id.action_edit -> {
                    findNavController(fragment).navigate(
                            BookFragmentDirections.actionBookFragmentToEditBookDialog(book.bookName)
                    )
                }
                R.id.action_archive -> {
                    findNavController(fragment).navigate(
                            BookFragmentDirections.actionBookFragmentToDialogArchiveBook(
                                    "封存題庫", "封存「${book.bookName}」?")
                    )
                }
                R.id.action_delete -> {
                    findNavController(fragment).navigate(
                            BookFragmentDirections.actionGlobalDialogDeleteCommon(
                                    "刪除題庫", "刪除「${book.bookName}」?")
                    )
                }


            }
            true
        }

        popup.show()
    }

    private fun initPopupWindow(v: View) {

        val linearLayout = LinearLayout(fragment.requireContext(), null, R.style.Theme_SelfStudy2).also {
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
                    R.style.Widget_App_Button_OutlinedButton_IconOnly
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = asyncListDiffer.currentList[position]

        with(holder.binding){
            bookNameTextView.text = book.bookName
            bookSizeTextView.text = "${book.size}"

            bookIcon.iconTint = ColorStateList.valueOf(book.colorInt)
        }

        tracker?.let {
            holder.itemView.isActivated = it.isSelected(book.id)
        }
    }

    override fun getItemCount() = asyncListDiffer.currentList.size
    override fun getItemId(position: Int): Long = asyncListDiffer.currentList[position].id

    companion object BookDiffCallback : DiffUtil.ItemCallback<Book>(){
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }

    fun submitList(bookList: List<Book>){
        asyncListDiffer.submitList(bookList)
    }

}
