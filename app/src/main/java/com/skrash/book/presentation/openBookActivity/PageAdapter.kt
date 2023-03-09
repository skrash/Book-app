package com.skrash.book.presentation.openBookActivity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skrash.book.R
import com.skrash.book.databinding.PageItemBinding


class PageAdapter :
    ListAdapter<Int, PageAdapter.PageAdapterHolder>(
        BookPageDiffCallback()
    ) {

    var renderPageImage: ((holder: PageAdapter.PageAdapterHolder, position: Int) -> Unit)? = null
    var onClickCallback: ((holder: PageAdapterHolder) -> Unit)? = null

    inner class PageAdapterHolder(
        val binding: ViewDataBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PageAdapter.PageAdapterHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            R.layout.page_item,
            parent,
            false
        )
        return PageAdapterHolder(binding)
    }

    override fun onBindViewHolder(holder: PageAdapter.PageAdapterHolder, position: Int) {
        when (holder.binding) {
            is PageItemBinding -> {
                holder.binding.ivMain.tag = position - 1
                renderPageImage?.invoke(holder, position)
                onClickCallback?.invoke(holder)
            }
        }
    }

    override fun onViewAttachedToWindow(holder: PageAdapterHolder) {
        onClickCallback?.invoke(holder)
        super.onViewAttachedToWindow(holder)
    }

}