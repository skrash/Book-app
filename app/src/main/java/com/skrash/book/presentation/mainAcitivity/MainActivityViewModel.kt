package com.skrash.book.presentation.mainAcitivity

import android.graphics.Bitmap
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import com.skrash.book.domain.entities.Genres
import com.skrash.book.domain.usecases.GetBookCoverUseCase
import com.skrash.book.domain.usecases.MyList.AddToMyBookListUseCase
import com.skrash.book.domain.usecases.MyList.DeleteBookItemFromMyListUseCase
import com.skrash.book.domain.usecases.MyList.GetMyBookListUseCase
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
    private val getMyBookListUseCase: GetMyBookListUseCase,
    private val deleteBookItemFromMyListUseCase: DeleteBookItemFromMyListUseCase,
    private val getBookCoverUseCase: GetBookCoverUseCase,
    private val addToMyBookListUseCase: AddToMyBookListUseCase
) : ViewModel() {

    val bookList = getMyBookListUseCase.getMyBookList()

    fun deleteShopItem(bookItem: BookItem) {
        viewModelScope.launch {
            deleteBookItemFromMyListUseCase.deleteBookItemFromMyList(bookItem)
        }
    }

    fun initializeFromDefaultPath() {
        val booksPathsArray = listOf<File>(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        )
        for (i in booksPathsArray) {
            val book = i.listFiles { _, fileName ->
                fileName.contains(".pdf") || fileName.contains(".PDF") || fileName.contains(".fb2") || fileName.contains(".FB2")
            }
            if (book != null){
                for (filePath in book) {
                    if (checkBookNotInMyList(filePath.absolutePath)){
                        val bookItem = compileDefaultBookItem(filePath.absolutePath, FormatBook.valueOf(filePath.absolutePath.substringAfterLast(".", "").uppercase()))
                        viewModelScope.launch {
                            addToMyBookListUseCase.addToMyBookList(bookItem)
                        }
                    }
                }
            }
        }
    }

    private fun compileDefaultBookItem(path: String, formatBook: FormatBook): BookItem {
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
                return BookItem(
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
            }
            FormatBook.FB2 -> {
                return BookItem(
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
            }
        }
    }

    private fun checkBookNotInMyList(path: String): Boolean{
        if (bookList.value != null){
            for(i in bookList.value!!){
                if(i.path == path){
                    return false
                }
            }
            return true
        } else {
            return true
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