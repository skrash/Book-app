package com.skrash.book.domain.usecases.MyList

import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class GetHashMyBookUseCase@Inject constructor(
    private val myBookItemRepository: MyBookItemRepository
)  {

    fun getHashMyBook(path: String): String{
        return myBookItemRepository.getHash(path)
    }
}