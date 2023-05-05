package com.skrash.book.domain.usecases.MyList

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.skrash.book.domain.entities.BookItem

interface MyBookItemRepository {

    suspend fun addToMyBookList(bookItem: BookItem): Long

    fun getMyBookList(): LiveData<List<BookItem>>

    suspend fun getMyBook(bookItemId: Int): BookItem

    suspend fun deleteBookItemFromMyList(bookItem: BookItem)

    suspend fun editMyBookItem(bookItem: BookItem)

    suspend fun updateStartOnPage(pageNum: Int, bookID: Int)

    suspend fun openBookItem(bookItem: BookItem)

    suspend fun getBookCover(bookItem: BookItem, width: Int, height: Int): Bitmap

    fun getPageBookItem(pageNum: Int, width: Int, height: Int): Bitmap

    fun getPageCount(bookItem: BookItem): Int

    fun getHash(path: String): String

    suspend fun getAllMyBookHashes(): List<String>

    suspend fun getMyBookItemByHash(hash: String): BookItem
}