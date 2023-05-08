package com.skrash.book.domain.usecases

import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class VoteUseCase @Inject constructor(
    private val bookItemRepository: BookItemRepository
)  {

    suspend fun vote(bookItem: BookItem, id: String, votePoint: Int){
        bookItemRepository.vote(bookItem, id, votePoint)
    }
}