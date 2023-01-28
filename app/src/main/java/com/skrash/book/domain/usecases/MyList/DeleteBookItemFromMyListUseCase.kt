package com.skrash.book.domain.usecases.MyList

import com.skrash.book.domain.BookItemRepository
import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class DeleteBookItemFromMyListUseCase @Inject constructor(
    private val bookItemRepository: BookItemRepository
) {

    suspend fun deleteBookItemFromMyList(bookItem: BookItem){
        return bookItemRepository.deleteBookItemFromMyList(bookItem)
    }
}