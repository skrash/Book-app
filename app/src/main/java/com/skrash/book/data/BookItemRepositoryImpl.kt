package com.skrash.book.data

import androidx.lifecycle.LiveData
import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class BookItemRepositoryImpl @Inject constructor(): BookItemRepository {

    private val bookList = mutableListOf<BookItem>()
    private var autoIncrement = 0

    override suspend fun addBookItem(bookItem: BookItem) {
        if (bookItem.id == BookItem.UNDEFINED_ID){
            bookItem.id = autoIncrement++
        }
        bookList.add(bookItem)
        updateGeneralBookList()
    }

    override suspend fun editBookItem(bookItem: BookItem) {
        bookList.remove(getBookItem(bookItem.id))
        addBookItem(bookItem)
    }

    override suspend fun getBookItem(bookItemId: Int): BookItem {
        return bookList.find {
            it.id == bookItemId
        } ?: throw RuntimeException("Not find element by id $bookItemId !")
    }

    override fun getBookItemList(): LiveData<List<BookItem>> {
        return BookListGeneral.bookListGeneral
    }

    override suspend fun openBookItem(bookItem: BookItem) {
        TODO("Not yet implemented")
    }

    private fun updateGeneralBookList(){
        BookListGeneral.bookListGeneral.postValue(bookList.toList())
    }
}