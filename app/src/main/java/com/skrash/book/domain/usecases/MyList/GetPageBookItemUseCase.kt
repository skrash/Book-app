package com.skrash.book.domain.usecases.MyList

import android.graphics.Bitmap
import javax.inject.Inject

class GetPageBookItemUseCase @Inject constructor(
    private val myBookItemRepository: MyBookItemRepository
)  {

    fun getPageBookItem(pageNum: Int, width: Int, height: Int): Bitmap{
        return myBookItemRepository.getPageBookItem(pageNum, width, height)
    }
}