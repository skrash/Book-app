package com.skrash.book.domain.usecases

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.skrash.book.data.network.model.BookItemDto
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import retrofit2.Call

interface BookItemRepository {
    suspend fun getBookItemList(): Call<List<BookItemDto>>

}