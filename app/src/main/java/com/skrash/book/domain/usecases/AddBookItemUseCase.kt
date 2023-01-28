package com.skrash.book.domain.usecases

import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class AddBookItemUseCase @Inject constructor(
    private val bookItemRepository: BookItemRepository
){

    suspend fun addBookItem(bookItem: BookItem){
        bookItemRepository.addBookItem(bookItem)
    }
}