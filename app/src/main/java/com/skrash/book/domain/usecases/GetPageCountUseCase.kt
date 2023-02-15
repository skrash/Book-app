package com.skrash.book.domain.usecases

import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class GetPageCountUseCase @Inject constructor(
    private val bookItemRepository: BookItemRepository
)   {

    fun getPageCount(bookItem: BookItem): Int{
        return bookItemRepository.getPageCount(bookItem)
    }
}