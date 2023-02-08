package com.skrash.book.domain.usecases.MyList

import javax.inject.Inject

class UpdateStartOnPageUseCase @Inject constructor(
    private val myBookItemRepository: MyBookItemRepository
) {

    suspend fun updateStartOnPage(pageNum: Int, bookID: Int){
        myBookItemRepository.updateStartOnPage(pageNum, bookID)
    }
}