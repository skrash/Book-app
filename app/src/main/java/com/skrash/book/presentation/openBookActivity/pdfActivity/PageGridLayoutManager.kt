package com.skrash.book.presentation.openBookActivity.pdfActivity

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class PageGridLayoutManager(context: Context) : LinearLayoutManager(context) {

    private var isScrollEnabled = true

    fun setScrollEnabled(flag: Boolean) {
        isScrollEnabled = flag
    }

    fun getScrollEnabled(): Boolean {
        return isScrollEnabled
    }

    override fun canScrollVertically(): Boolean {
        return isScrollEnabled && super.canScrollVertically()
    }
}