package com.skrash.book.presentation.mainAcitivity

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import com.skrash.book.FormatBook.FB2
import com.skrash.book.data.network.model.BookItemDtoMapper
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import com.skrash.book.domain.entities.Genres
import com.skrash.book.domain.usecases.GetBookItemListUseCase
import com.skrash.book.domain.usecases.MyList.GetBookCoverUseCase
import com.skrash.book.domain.usecases.MyList.AddToMyBookListUseCase
import com.skrash.book.domain.usecases.MyList.DeleteBookItemFromMyListUseCase
import com.skrash.book.domain.usecases.MyList.GetMyBookListUseCase
import kotlinx.coroutines.*
import java.io.File
import java.net.ConnectException
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
    private val getMyBookListUseCase: GetMyBookListUseCase,
    private val deleteBookItemFromMyListUseCase: DeleteBookItemFromMyListUseCase,
    private val getBookCoverUseCase: GetBookCoverUseCase,
    private val addToMyBookListUseCase: AddToMyBookListUseCase,
    private val getBookItemListUseCase: GetBookItemListUseCase
) : ViewModel() {

    private val _myBookList = MediatorLiveData<List<BookItem>>()
    val bookList: LiveData<List<BookItem>>
        get() = _myBookList
    private val _netBookList = MutableLiveData<List<BookItem>>()
    val netBookList: LiveData<List<BookItem>>
        get() = _netBookList

    fun initMyBook(errorCallback: () -> Unit) {
        _myBookList.addSource(getMyBookListUseCase.getMyBookList()){
            _myBookList.value = it
        }
        getListBooks(errorCallback)
    }

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
                val bookFile = File(path)
                val metaInfo = FB2.getMetaInfo(bookFile)
                val book = BookItem(
                    title = metaInfo.title,
                    author = metaInfo.author,
                    description = "",
                    rating = 0.0f,
                    popularity = 0.0f,
                    Genres.Other,
                    tags = metaInfo.tag,
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

    fun getListBooks(errorCallback: () -> Unit) {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            if (throwable is ConnectException) {
                CoroutineScope(Dispatchers.Main).launch {
                    errorCallback.invoke()
                    _netBookList.value = listOf()
                }
            }
            Log.d("ERRORS", throwable.localizedMessage)
            throwable.printStackTrace()
        }
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            var bookItems: List<BookItem> = listOf()
            val bookItemsDto = getBookItemListUseCase.getBookItemList()
            if (bookItemsDto.execute().body() != null) {
                bookItems = BookItemDtoMapper.bookItemDtoListToBookItemList(
                    bookItemsDto.clone().execute().body()!!
                )
            }
            _netBookList.postValue(bookItems)
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