package com.skrash.book.domain.usecases.MyList

import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class GetMyBookUseCase @Inject constructor(
    private val myBookItemRepository: MyBookItemRepository
){

    suspend fun getMyBook(bookItemId: Int): BookItem {
        return myBookItemRepository.getMyBook(bookItemId)
    }
}