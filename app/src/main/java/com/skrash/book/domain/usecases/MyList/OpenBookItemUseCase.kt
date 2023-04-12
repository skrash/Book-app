package com.skrash.book.domain.usecases.MyList

import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class OpenBookItemUseCase @Inject constructor(
    private val myBookItemRepository: MyBookItemRepository
) {

    suspend fun openBookItem(bookItem: BookItem){
        myBookItemRepository.openBookItem(bookItem)
    }
}