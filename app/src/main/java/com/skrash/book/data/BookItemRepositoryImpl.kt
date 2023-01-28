package com.skrash.book.data

import androidx.lifecycle.LiveData
import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class BookItemRepositoryImpl @Inject constructor(): BookItemRepository {

    private val bookList = mutableListOf<BookItem>()
    private var autoIncrement = 0
    private val myBookList = mutableListOf<BookItem>()

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

    override suspend fun addToMyBookList(bookItem: BookItem) {
        if (!myBookList.contains(bookItem))
        {
            bookList.add(bookItem)
            updateMyBookList()
        }
    }

    override suspend fun getMyBookList(): LiveData<List<BookItem>> {
        return MyBookListObj.myBookList
    }

    override suspend fun getMyBook(bookItemId: Int): BookItem {
        return myBookList.find {
            it.id == bookItemId
        } ?: throw RuntimeException("Not find element by id $bookItemId !")
    }

    override suspend fun deleteBookItemFromMyList(bookItem: BookItem) {
        myBookList.remove(bookItem)
        updateMyBookList()
    }

    override suspend fun openBookItem(bookItem: BookItem) {
        TODO("Not yet implemented")
    }

    private fun updateGeneralBookList(){
        BookListGeneral.bookListGeneral.postValue(bookList.toList())
    }

    private fun updateMyBookList(){
        BookListGeneral.bookListGeneral.postValue(myBookList.toList())
    }
}