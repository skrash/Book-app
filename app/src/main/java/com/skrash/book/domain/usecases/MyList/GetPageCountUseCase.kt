package com.skrash.book.domain.usecases.MyList

import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class GetPageCountUseCase @Inject constructor(
    private val myBookItemRepository: MyBookItemRepository
)   {

    fun getPageCount(bookItem: BookItem): Int{
        return myBookItemRepository.getPageCount(bookItem)
    }
}