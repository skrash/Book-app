package com.skrash.book.domain.usecases.MyList

import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class DeleteBookItemFromMyListUseCase @Inject constructor(
    private val myBookItemRepository: MyBookItemRepository
) {

    suspend fun deleteBookItemFromMyList(bookItem: BookItem){
        return myBookItemRepository.deleteBookItemFromMyList(bookItem)
    }
}