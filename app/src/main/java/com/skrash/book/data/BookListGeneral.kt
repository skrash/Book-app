package com.skrash.book.data

import androidx.lifecycle.MutableLiveData
import com.skrash.book.domain.entities.BookItem

object BookListGeneral {
    val bookListGeneral = MutableLiveData<List<BookItem>>()
}