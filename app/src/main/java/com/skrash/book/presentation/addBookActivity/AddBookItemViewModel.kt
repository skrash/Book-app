package com.skrash.book.presentation.addBookActivity

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import com.skrash.book.domain.entities.Genres
import com.skrash.book.domain.usecases.MyList.AddToMyBookListUseCase
import com.skrash.book.domain.usecases.MyList.GetBookCoverUseCase
import com.skrash.book.domain.usecases.MyList.GetMyBookUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class AddBookItemViewModel @Inject constructor(
    private val addToMyBookListUseCase: AddToMyBookListUseCase,
    private val getMyBookUseCase: GetMyBookUseCase,
    private val getBookCoverUseCase: GetBookCoverUseCase
) : ViewModel() {

    private val _imageCover = MutableLiveData<Bitmap>()
    val imageCover
        get() = _imageCover

    private val _bookItem = MutableLiveData<BookItem>()
    val bookItem: LiveData<BookItem>
        get() = _bookItem

    private val _shouldCloseScreen = MutableLiveData<Unit>()
    val shouldCloseScreen: LiveData<Unit>
        get() = _shouldCloseScreen

    private val _itemIdManipulated = MutableLiveData<Int>()
    val itemIdManipulated: LiveData<Int>
        get() = _itemIdManipulated

    // VALIDATORS

    private val _errorInputTitle = MutableLiveData<Boolean>()
    val errorInputTitle: LiveData<Boolean>
        get() = _errorInputTitle

    private val _errorInputAuthor = MutableLiveData<Boolean>()
    val errorInputAuthor: LiveData<Boolean>
        get() = _errorInputAuthor

    private val _errorInputDescription = MutableLiveData<Boolean>()
    val errorInputDescription: LiveData<Boolean>
        get() = _errorInputDescription

    private val _errorInputGenres = MutableLiveData<Boolean>()
    val errorInputGenres: LiveData<Boolean>
        get() = _errorInputGenres

    private val _errorInputTags = MutableLiveData<Boolean>()
    val errorInputTags: LiveData<Boolean>
        get() = _errorInputTags

    private val _errorInputPath = MutableLiveData<Boolean>()
    val errorInputPath: LiveData<Boolean>
        get() = _errorInputPath
    // ======================================

    private suspend fun addBookItem(bookItem: BookItem): Long {
        val deferred = CoroutineScope(Dispatchers.IO).async {
            val id = addToMyBookListUseCase.addToMyBookList(bookItem)
            id
        }
        return deferred.await()
    }

    fun finishEditing(
        id: Int? = null,
        title: String?,
        author: String?,
        description: String?,
        genres: String?,
        tags: String?,
        path: String?,
        shareAccess: Boolean
    ) {
        if (title == null) {
            _errorInputTitle.value = true
            return
        }
        if (title.length > 13000) {
            _errorInputTitle.value = true
            return
        }
        if (author == null) {
            _errorInputAuthor.value = true
            return
        }
        if (author.length > 50) {
            _errorInputAuthor.value = true
            return
        }
        if (description == null) {
            _errorInputDescription.value = true
            return
        }
        if (description.length > 10000) {
            _errorInputDescription.value = true
            return
        }
        if (genres == null) {
            _errorInputGenres.value = true
            return
        }
        try {
            Genres.valueOf(genres)
        } catch (e: java.lang.Exception) {
            _errorInputGenres.value = true
            return
        }
        val genresParsed = Genres.valueOf(genres)
        if (tags == null) {
            _errorInputTags.value = true
            return
        }
        if (!tags.matches(Regex("[а-яё]+|[а-яё][, \\s[а-яё]]+"))) {
            _errorInputTags.value = true
            return
        }
        if (path == null) {
            _errorInputPath.value = true
            return
        }
        try {
            File(path)
        } catch (e: Exception) {
            _errorInputPath.value = true
            return
        }
        val fileExtension = path.substringAfterLast('.', "").lowercase()
        if (!FormatBook.values().map { it.string_name }.contains(fileExtension)) {
            _errorInputPath.value = true
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            if (id == null) {
                val id = addBookItem(
                    BookItem(
                        title = title.trim(),
                        author = author.trim(),
                        description = description.trim(),
                        rating = 0.0f,
                        popularity = 0.0f,
                        genres = genresParsed,
                        tags = tags.trim(),
                        path = path,
                        startOnPage = 0,
                        shareAccess = shareAccess,
                        fileExtension = fileExtension,
                    )
                )
                Log.d("TEST_WORKER", "added id ${id.toInt()}")
                _itemIdManipulated.postValue(id.toInt())
            } else {
                addBookItem(
                    BookItem(
                        id = id,
                        title = title.trim(),
                        author = author.trim(),
                        description = description.trim(),
                        rating = 0.0f,
                        popularity = 0.0f,
                        genres = genresParsed,
                        tags = tags.trim(),
                        path = path,
                        startOnPage = 0,
                        shareAccess = shareAccess,
                        fileExtension = fileExtension,
                    )
                )
                Log.d("TEST_WORKER", "added id ${id.toInt()}")
                _itemIdManipulated.value = id.toInt()
            }
        }


        finishWork()
    }

    fun getCover(bookItem: BookItem, width: Int, height: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val item = getBookCoverUseCase.getBookCover(bookItem, width, height)
            _imageCover.postValue(item)
        }
    }

    fun getBookItem(bookItemId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val item = getMyBookUseCase.getMyBook(bookItemId)
            _bookItem.postValue(item)
        }
    }

    fun finishWork() {
        _shouldCloseScreen.value = Unit
    }

    fun resetErrorInputTitle() {
        _errorInputTitle.value = false
    }

    fun resetErrorInputAuthor() {
        _errorInputAuthor.value = false
    }

    fun resetErrorInputDescription() {
        _errorInputDescription.value = false
    }

    fun resetErrorInputGenres() {
        _errorInputGenres.value = false
    }

    fun resetErrorInputTags() {
        _errorInputTags.value = false
    }

    fun resetErrorInputPath() {
        _errorInputPath.value = false
    }
}