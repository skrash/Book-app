package com.skrash.book.domain.usecases

import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class GetBookItemUseCase @Inject constructor(
    private val bookItemRepository: BookItemRepository
) {

    suspend fun getBookItem(bookItemId: Int): BookItem{
        return bookItemRepository.getBookItem(bookItemId)
    }
}