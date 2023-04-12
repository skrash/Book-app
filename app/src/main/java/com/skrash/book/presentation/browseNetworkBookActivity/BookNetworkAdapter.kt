package com.skrash.book.presentation.browseNetworkBookActivity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skrash.book.R
import com.skrash.book.data.network.model.BookItemDto
import com.skrash.book.databinding.BookItemNetworkBinding

class BookNetworkAdapter : ListAdapter<BookItemDto, BookNetworkAdapter.BookItemNetworkViewHolder>(
    BookItemNetworkDiffUtilCallback()
) {

    inner class BookItemNetworkViewHolder(
        val binding: ViewDataBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookItemNetworkViewHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            R.layout.book_item_network,
            parent,
            false
        )
        return BookItemNetworkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookItemNetworkViewHolder, position: Int) {
        val bookItem = getItem(position)
        val binding = holder.binding
        when (binding) {
            is BookItemNetworkBinding -> {
                binding.bookItem = bookItem
            }
        }
    }
}