package com.skrash.book.domain.usecases.MyList

import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class GetMyBookItemByHash @Inject constructor(
    private val myBookItemRepository: MyBookItemRepository
)   {

    suspend fun getMyBookItemByHash(hash: String): BookItem{
        return myBookItemRepository.getMyBookItemByHash(hash)
    }
}