package com.skrash.book.presentation.bookInfoActivity

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import com.skrash.book.domain.usecases.GetBookCoverUseCase
import com.skrash.book.domain.usecases.MyList.GetMyBookUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class BookInfoViewModel @Inject constructor(
    private val getMyBookUseCase: GetMyBookUseCase,
    private val getBookCoverUseCase: GetBookCoverUseCase
) : ViewModel() {
    private val _bookItem = MutableLiveData<BookItem>()
    val bookItem: LiveData<BookItem>
        get() = _bookItem

    private val _imgCover = MutableLiveData<Bitmap>()
    val imgCover
    get() = _imgCover

    fun getBookItem(bookItemId: Int) {
        viewModelScope.launch {
            val item = getMyBookUseCase.getMyBook(bookItemId)
            _bookItem.value = item
            loadCover()
        }
    }

    fun loadCover() {
        viewModelScope.launch {
            if (_bookItem.value != null) {
                _imgCover.value = getBookCoverUseCase.getBookCover(
                    FormatBook.valueOf(_bookItem.value!!.fileExtension.uppercase()),
                    _bookItem.value!!.path,
                    300,
                    300
                )
            }
        }
    }
}