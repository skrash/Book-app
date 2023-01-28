package com.skrash.book.data

import androidx.lifecycle.LiveData
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.usecases.MyList.MyBookItemRepository
import javax.inject.Inject

class MyBookItemRepositoryImpl @Inject constructor(): MyBookItemRepository {

    private val myBookList = mutableListOf<BookItem>()
    private var autoIncrement = 0

    override suspend fun addToMyBookList(bookItem: BookItem) {
        if (!myBookList.contains(bookItem))
        {
            myBookList.add(bookItem)
            updateMyBookList()
        }
    }

    override fun getMyBookList(): LiveData<List<BookItem>> {
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

    private fun updateMyBookList(){
        MyBookListObj.myBookList.postValue(myBookList.toList())
    }

}