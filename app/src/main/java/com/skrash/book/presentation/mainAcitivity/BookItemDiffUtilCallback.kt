package com.skrash.book.presentation.mainAcitivity

import androidx.recyclerview.widget.DiffUtil
import com.skrash.book.domain.entities.BookItem

class BookItemDiffUtilCallback: DiffUtil.ItemCallback<BookItem>() {

    override fun areItemsTheSame(oldItem: BookItem, newItem: BookItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BookItem, newItem: BookItem): Boolean {
        return oldItem == newItem
    }


}