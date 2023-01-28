package com.skrash.book.domain.usecases.MyList

import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class GetMyBookUseCase @Inject constructor(
    private val bookItemRepository: BookItemRepository
){

    suspend fun getMyBook(bookItemId: Int): BookItem {
        return bookItemRepository.getMyBook(bookItemId)
    }
}