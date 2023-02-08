package com.skrash.book.domain.usecases.MyList

import androidx.lifecycle.LiveData
import com.skrash.book.domain.entities.BookItem

interface MyBookItemRepository {

    suspend fun addToMyBookList(bookItem: BookItem)

    fun getMyBookList(): LiveData<List<BookItem>>

    suspend fun getMyBook(bookItemId: Int): BookItem

    suspend fun deleteBookItemFromMyList(bookItem: BookItem)

    suspend fun editMyBookItem(bookItem: BookItem)

    suspend fun updateStartOnPage(pageNum: Int, bookID: Int)

}