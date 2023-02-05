package com.skrash.book.presentation.openBookActivity

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skrash.book.R
import com.skrash.book.databinding.PageItemBinding


class PageAdapter(private val viewModel: OpenBookViewModel, private val width: Int, private val height: Int) :
    ListAdapter<Int, PageAdapter.PageAdapterHolder>(
        BookPageDiffCallback()
    ) {

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
        when (val binding = holder.binding) {
            is PageItemBinding -> {
                if (viewModel.pdfRenderer != null) {
                    val page = viewModel.pdfRenderer!!.openPage(position)
                    val bitmap = Bitmap.createBitmap(
                        width, height,
                        Bitmap.Config.ARGB_4444
                    )
                    page!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    binding.ivMain.setImageBitmap(bitmap)
                    page.close()
                }
            }
        }
    }
}