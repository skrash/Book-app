package com.skrash.book.domain

import androidx.lifecycle.LiveData
import com.skrash.book.domain.entities.BookItem

interface BookItemRepository {

    suspend fun addBookItem(bookItem: BookItem)

    suspend fun editBookItem(bookItem: BookItem)

    suspend fun getBookItem(bookItemId: Int): BookItem

    fun getBookItemList(): LiveData<List<BookItem>>

    suspend fun addToMyBookList(bookItem: BookItem)

    suspend fun getMyBookList(): LiveData<List<BookItem>>

    suspend fun getMyBook(bookItemId: Int): BookItem

    suspend fun deleteBookItemFromMyList(bookItem: BookItem)

    suspend fun openBookItem(bookItem: BookItem)

}