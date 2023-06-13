package com.skrash.book.domain.usecases.MyList

import javax.inject.Inject

class GetAllMyBookHashes @Inject constructor(
    private val myBookItemRepository: MyBookItemRepository
)  {

    suspend fun getAllMyBookHashes(): List<String>{
        return myBookItemRepository.getAllMyBookHashes()
    }
}