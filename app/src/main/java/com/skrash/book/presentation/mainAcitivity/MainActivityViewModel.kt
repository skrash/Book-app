package com.skrash.book.presentation.mainAcitivity

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import com.skrash.book.domain.entities.Genres
import com.skrash.book.domain.usecases.AddBookItemUseCase
import com.skrash.book.domain.usecases.GetBookCoverUseCase
import com.skrash.book.domain.usecases.GetBookItemListUseCase
import com.skrash.book.domain.usecases.MyList.AddToMyBookListUseCase
import com.skrash.book.domain.usecases.MyList.DeleteBookItemFromMyListUseCase
import com.skrash.book.domain.usecases.MyList.GetMyBookListUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

class MainActivityViewModel @Inject constructor(
    private val getMyBookListUseCase: GetMyBookListUseCase,
    private val deleteBookItemFromMyListUseCase: DeleteBookItemFromMyListUseCase,
    private val getBookCoverUseCase: GetBookCoverUseCase
) : ViewModel() {

    val bookList = getMyBookListUseCase.getMyBookList()

    fun deleteShopItem(bookItem: BookItem){
        viewModelScope.launch {
            deleteBookItemFromMyListUseCase.deleteBookItemFromMyList(bookItem)
        }
    }

    suspend fun getBookCover(formatBook: FormatBook, path: String, width: Int, height: Int): Bitmap{
        return getBookCoverUseCase.getBookCover(formatBook, path, width, height)
    }
}