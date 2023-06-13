package com.skrash.book.presentation.openBookActivity.pdfActivity

import androidx.recyclerview.widget.DiffUtil

class BookPageDiffCallback: DiffUtil.ItemCallback<Int>() {
    override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
        return false
    }
}