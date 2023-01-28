package com.skrash.book.data

import androidx.lifecycle.MutableLiveData
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.Genres

object MyBookListObj {
    val myBookList = MutableLiveData<List<BookItem>>()
}