package com.skrash.book.domain

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook

interface BookItemRepository {

    suspend fun addBookItem(bookItem: BookItem)

    suspend fun editBookItem(bookItem: BookItem)

    suspend fun getBookItem(bookItemId: Int): BookItem

    fun getBookItemList(): LiveData<List<BookItem>>

    suspend fun openBookItem(path: String, type: FormatBook): Any

}