package com.skrash.book.domain.usecases.MyList

import android.graphics.Bitmap
import com.skrash.book.domain.entities.BookItem
import javax.inject.Inject

class GetBookCoverUseCase @Inject constructor(
    private val myBookItemRepository: MyBookItemRepository
) {

    suspend fun getBookCover(bookItem: BookItem, width: Int, height: Int): Bitmap {
        return myBookItemRepository.getBookCover(bookItem, width, height)
    }
}