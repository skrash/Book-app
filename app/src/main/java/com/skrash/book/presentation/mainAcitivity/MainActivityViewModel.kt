package com.skrash.book.presentation.mainAcitivity

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import com.skrash.book.data.network.model.BookItemDto
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import com.skrash.book.domain.entities.Genres
import com.skrash.book.domain.usecases.GetBookItemListUseCase
import com.skrash.book.domain.usecases.MyList.GetBookCoverUseCase
import com.skrash.book.domain.usecases.MyList.AddToMyBookListUseCase
import com.skrash.book.domain.usecases.MyList.DeleteBookItemFromMyListUseCase
import com.skrash.book.domain.usecases.MyList.GetMyBookListUseCase
import kotlinx.coroutines.*
import java.net.ConnectException
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
    private val getMyBookListUseCase: GetMyBookListUseCase,
    private val deleteBookItemFromMyListUseCase: DeleteBookItemFromMyListUseCase,
    private val getBookCoverUseCase: GetBookCoverUseCase,
    private val addToMyBookListUseCase: AddToMyBookListUseCase,
    private val getBookItemListUseCase: GetBookItemListUseCase
) : ViewModel() {

    private val _bookList = MediatorLiveData<List<BookItem>>()
    val bookList: LiveData<List<BookItem>>
        get() = _bookList

    init {
        CoroutineScope(Dispatchers.IO).launch {
            _bookList.addSource(getMyBookListUseCase.getMyBookList()){
                _bookList.value = it
            }
        }
    }

    private val _bookListNet = MutableLiveData<List<BookItemDto>>()
    val bookListNet: LiveData<List<BookItemDto>>
        get() = _bookListNet

    var mode = MainActivity.MODE_MY_BOOK

    fun deleteShopItem(bookItem: BookItem) {
        CoroutineScope(Dispatchers.IO).launch {
            deleteBookItemFromMyListUseCase.deleteBookItemFromMyList(bookItem)
        }
    }

    fun compileDefaultBookItem(path: String, formatBook: FormatBook) {
        when (formatBook) {
            FormatBook.PDF -> {
                val fileName = path.substringAfterLast("/")
                val regexAuthor = "[A-ZА-ЯЁa-zа-яё]+ ([A-ZА-ЯЁ]{1}[.]){1,2}".toRegex()
                val tryAuthor = regexAuthor.findAll(fileName)
                var authorString = ""
                var title = fileName
                for (i in tryAuthor) {
                    title = title.replace(i.value, "")
                    if (i.value != "") {
                        authorString += "${i.value},"
                    }
                }
                title = title.replace("." + title.substringAfterLast(".", ""), "")
                title = title.replace("[,.-]+".toRegex(), "")
                title = title.trim()
                val book = BookItem(
                    title = title,
                    author = authorString,
                    description = "",
                    rating = 0.0f,
                    popularity = 0.0f,
                    Genres.Other,
                    tags = "",
                    path = path,
                    fileExtension = FormatBook.PDF.string_name,
                    startOnPage = 0
                )
                CoroutineScope(Dispatchers.IO).launch {
                    addToMyBookListUseCase.addToMyBookList(book)
                }
            }
            FormatBook.FB2 -> {
                val book = BookItem(
                    title = "",
                    author = "",
                    description = "",
                    rating = 0.0f,
                    popularity = 0.0f,
                    Genres.Other,
                    tags = "",
                    path = path,
                    fileExtension = FormatBook.FB2.string_name,
                    startOnPage = 0
                )
                CoroutineScope(Dispatchers.IO).launch {
                    addToMyBookListUseCase.addToMyBookList(book)
                }
            }
        }
    }

    fun getListBooks(errorCallback: () -> Unit, successCallback: () -> Unit) {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            if (throwable is ConnectException){
                CoroutineScope(Dispatchers.Main).launch {
                    errorCallback.invoke()
                }
            }
            Log.d("ERRORS", throwable.localizedMessage)
        }
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch{
            if (_bookListNet.value == null) {
                val bookItem = getBookItemListUseCase.getBookItemList()
                _bookListNet.postValue(bookItem.execute().body())
            }
            CoroutineScope(Dispatchers.Main).launch {
                successCallback.invoke()
            }
        }
    }

    suspend fun getBookCover(
        bookItem: BookItem,
        width: Int,
        height: Int
    ): Bitmap {
        return getBookCoverUseCase.getBookCover(bookItem, width, height)
    }
}