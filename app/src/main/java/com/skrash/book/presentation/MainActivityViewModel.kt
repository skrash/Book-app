package com.skrash.book.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.Genres
import com.skrash.book.domain.usecases.AddBookItemUseCase
import com.skrash.book.domain.usecases.GetBookItemListUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
    private val getBookItemList: GetBookItemListUseCase,
    private val addBookItemUseCase: AddBookItemUseCase
) : ViewModel() {

    val bookList = getBookItemList.getBookItemList()

    init {
        viewModelScope.launch {
            for( i in 0 until 10) {
                addBookItemUseCase.addBookItem(
                    BookItem(
                        i.toString(),
                        i.toString(),
                        i.toString(),
                        i.toFloat(),
                        i.toFloat(),
                        Genres.nan,
                        "1,2,3,4,5,6",
                        "img",
                        ".pdf",
                        i
                    )
                )
            }
        }
    }
}