package com.skrash.book.presentation.mainAcitivity

import androidx.recyclerview.widget.DiffUtil
import com.skrash.book.data.network.model.BookItemDto
import com.skrash.book.domain.entities.BookItem

class BookItemNetworkDiffUtilCallback: DiffUtil.ItemCallback<BookItemDto>() {

    override fun areItemsTheSame(oldItem: BookItemDto, newItem: BookItemDto): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: BookItemDto, newItem: BookItemDto): Boolean {
        return oldItem == newItem
    }


}