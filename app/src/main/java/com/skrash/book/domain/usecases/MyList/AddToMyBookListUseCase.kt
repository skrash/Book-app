package com.skrash.book.domain.usecases.MyList

import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class AddToMyBookListUseCase @Inject constructor(
    private val myBookItemRepository: MyBookItemRepository
) {

    suspend fun addToMyBookList(bookItem: BookItem){
        myBookItemRepository.addToMyBookList(bookItem)
    }
}