package com.skrash.book.presentation.bookInfoActivity

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.Genres
import com.skrash.book.domain.usecases.MyList.GetBookCoverUseCase
import com.skrash.book.domain.usecases.MyList.GetMyBookUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        CoroutineScope(Dispatchers.IO).launch {
            val item = getMyBookUseCase.getMyBook(bookItemId)
            _bookItem.postValue(item)
            loadCover()
        }
    }

    fun setNetBook(
        title: String,
        description: String,
        author: String,
        genres: String,
        tags: String,
        popularity: Float,
        rating: Float
    ) {
        _bookItem.value = BookItem(
            title = title,
            author = author,
            description = description,
            genres = Genres.valueOf(genres),
            tags = tags,
            popularity = popularity,
            rating = rating,
            fileExtension = "",
            path = "network",
            startOnPage = -1
        )
    }

    private fun loadCover() {
        CoroutineScope(Dispatchers.IO).launch {
            if (_bookItem.value != null) {
                val item = getBookCoverUseCase.getBookCover(
                    BookItem(
                        id = -1,
                        title = "",
                        author = "",
                        description = "",
                        rating = 0.0f,
                        popularity = 0.0f,
                        genres = Genres.Other,
                        tags = "",
                        path = _bookItem.value!!.path,
                        startOnPage = 0,
                        fileExtension = _bookItem.value!!.fileExtension.uppercase(),
                    ),
                    300,
                    500
                )
                _imgCover.postValue(item)
            }
        }
    }

    fun downloadBook() {

    }
}