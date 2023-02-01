package com.skrash.book.domain.usecases.MyList

import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class EditMyBookItemUseCase @Inject constructor(
    private val myBookItemRepository: MyBookItemRepository
) {
    suspend fun editMyBookItem(bookItem: BookItem){
        myBookItemRepository.editMyBookItem(bookItem)
    }
}