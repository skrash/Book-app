package com.skrash.book.presentation.mainAcitivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skrash.book.R
import com.skrash.book.databinding.BookItemBinding
import com.skrash.book.domain.entities.BookItem

class BookListAdapter : ListAdapter<BookItem, BookListAdapter.BookItemViewHolder>(
    BookItemDiffUtilCallback()
){

    var onBookItemClickListener: ((BookItem) -> Unit)? = null
    var onEditBookClickListener: ((BookItem) -> Unit)? = null
    var loadCoverFunction: ((BookItemViewHolder, BookItem) -> Unit)? = null

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
        binding.root.setOnClickListener {
            onBookItemClickListener?.invoke(bookItem)
        }
        loadCoverFunction?.invoke(holder, bookItem)
        when(binding){
            is BookItemBinding -> {
                if (bookItem.shareAccess){
                    binding.ivShareAccess.setImageResource(R.drawable.ic_baseline_cloud_upload_24_green)
                } else {
                    binding.ivShareAccess.setImageResource(R.drawable.ic_baseline_cloud_upload_24)
                }
                binding.ivShareAccess.visibility = View.VISIBLE
                binding.btnEdit.setOnClickListener {
                    onEditBookClickListener?.invoke(bookItem)
                }
                binding.bookItem = bookItem
            }
        }
    }
}