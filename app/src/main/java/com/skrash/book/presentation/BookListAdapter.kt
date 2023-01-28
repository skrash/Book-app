package com.skrash.book.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skrash.book.R
import com.skrash.book.databinding.BookItemBinding
import com.skrash.book.domain.entities.BookItem

class BookListAdapter : ListAdapter<BookItem, BookListAdapter.BookItemViewHolder>(BookItemDiffUtilCallback()){


    inner class BookItemViewHolder(
        val binding: ViewDataBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookItemViewHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            R.layout.book_item,
            parent,
            false
        )
        return BookItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookItemViewHolder, position: Int) {
        val bookItem = getItem(position)
        val binding = holder.binding
        when(binding){
            is BookItemBinding -> {
                binding.bookItem = bookItem
            }
        }
    }
}