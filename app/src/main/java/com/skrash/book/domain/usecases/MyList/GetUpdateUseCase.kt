package com.skrash.book.domain.usecases.MyList

import com.skrash.book.data.network.model.UpdateItemDto
import retrofit2.Call
import javax.inject.Inject

class GetUpdateUseCase @Inject constructor(
    private val myBookItemRepository: MyBookItemRepository
)    {

    suspend fun getUpdate(userID: String){
        myBookItemRepository.getUpdate(userID)
    }
}