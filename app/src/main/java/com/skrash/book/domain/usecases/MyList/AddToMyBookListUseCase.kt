package com.skrash.book.domain.usecases.MyList

import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class AddToMyBookListUseCase @Inject constructor(
    private val bookItemRepository: BookItemRepository
) {

    suspend fun addToBookList(bookItem: BookItem){
        bookItemRepository.addToMyBookList(bookItem)
    }
}