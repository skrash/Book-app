package com.skrash.book.domain.usecases

import android.graphics.Bitmap
import com.skrash.book.domain.BookItemRepository
import javax.inject.Inject

class GetPageBookItemUseCase @Inject constructor(
    private val bookItemRepository: BookItemRepository
)  {

    fun getPageBookItem(pageNum: Int, width: Int, height: Int): Bitmap{
        return bookItemRepository.getPageBookItem(pageNum, width, height)
    }
}