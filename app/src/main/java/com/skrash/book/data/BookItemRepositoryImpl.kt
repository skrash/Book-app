package com.skrash.book.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.skrash.book.data.network.ApiFactory
import com.skrash.book.data.network.model.BookItemDto
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.usecases.BookItemRepository
import retrofit2.Call
import javax.inject.Inject

class BookItemRepositoryImpl @Inject constructor(): BookItemRepository {

    override suspend fun getBookItemList(): Call<List<BookItemDto>> {
        return ApiFactory.apiService.listBook()
    }
}