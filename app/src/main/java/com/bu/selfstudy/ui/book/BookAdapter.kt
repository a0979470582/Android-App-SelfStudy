package com.bu.selfstudy.ui.book

import android.content.res.ColorStateList
import android.view.*
import android.view.LayoutInflater
import android.view.ViewGroup
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
            //findNavController(fragment).navigate(R.id.dialogChooseColor)
            initPopupWindow(v)
        }

        holder.binding.moreIcon.setOnClickListener { v: View ->
            showMenu(v, R.menu.book_action_mode,
                    asyncListDiffer.currentList[holder.adapterPosition])
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

        fragment.setLongPressedBook(book)
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
        val view: View = LayoutInflater.from(fragment.requireContext()).inflate(
                R.layout.book_color_list_item, null)
        val popupWindow = PopupWindow(view)
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
        popupWindow.setFocusable(false)

        popupWindow.showAsDropDown(v)

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
