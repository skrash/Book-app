package com.skrash.book.domain.usecases

import androidx.lifecycle.LiveData
import com.skrash.book.data.network.model.BookItemDto
import com.skrash.book.domain.entities.BookItem
import retrofit2.Call
import javax.inject.Inject

class GetBookItemListUseCase @Inject constructor(
    private val bookItemRepository: BookItemRepository
) {

    suspend fun getBookItemList(userID: String): Call<List<BookItemDto>> {
        return bookItemRepository.getBookItemList(userID)
    }
}