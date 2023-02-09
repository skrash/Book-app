package com.skrash.book.presentation.mainAcitivity

import android.graphics.Bitmap
import android.os.Environment
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
import com.skrash.book.presentation.addBookActivity.AddBookItemFragment
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileFilter
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

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
        )
        for (i in booksPathsArray) {
            val book = i.listFiles { _, fileName ->
                fileName.contains(".pdf") || fileName.contains(".PDF")
            }
            for (filePath in book!!) {
                if (checkBookNotInMyList(filePath.absolutePath)){
                    val bookItem = compileDefaultBookItem(filePath.absolutePath, FormatBook.PDF)
                    viewModelScope.launch {
                        addToMyBookListUseCase.addToMyBookList(bookItem)
                    }
                }
            }
        }
    }

    private fun compileDefaultBookItem(path: String, formatBook: FormatBook): BookItem {
        when (formatBook) {
            FormatBook.PDF -> {
                val fileName = path.substringAfterLast("/")
                Log.d("TEST20", "fileName: $fileName")
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
                    Genres.nan,
                    tags = "",
                    cover = "",
                    path = path,
                    fileExtension = FormatBook.PDF.string_name,
                    startOnPage = 0
                )
            }
        }
    }

    private fun checkBookNotInMyList(path: String): Boolean{
        Log.d("TEST31", (bookList.value == null).toString())
        if (bookList.value != null){
            for(i in bookList.value!!){
                Log.d("TEST30", "path in db = ${i.path}, try adding = $path")
                if(i.path == path){
                    return false
                }
            }
            return true
        } else {
            Log.d("TEST30", "bookList is null")
            return true
        }
    }

    suspend fun getBookCover(
        formatBook: FormatBook,
        path: String,
        width: Int,
        height: Int
    ): Bitmap {
        return getBookCoverUseCase.getBookCover(formatBook, path, width, height)
    }
}