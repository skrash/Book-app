package com.skrash.book.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.skrash.book.data.myBook.MyBookListDao
import com.skrash.book.BookApplication
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.Genres
import com.skrash.book.domain.usecases.MyList.AddToMyBookListUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class BookProvider : ContentProvider() {

    private val component by lazy {
        (context as BookApplication).component
    }

    @Inject
    lateinit var bookListDao: MyBookListDao

    @Inject
    lateinit var addToMyBookListUseCase: AddToMyBookListUseCase

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, "book/#", GET_BOOK_BY_ID)
        addURI(AUTHORITY, "shared", GET_SHARED_BOOKS)
        addURI(AUTHORITY, "create", CREATE_BOOK)
    }

    override fun onCreate(): Boolean {
        component.inject(this)
        return true
    }

    override fun insert(uri: Uri, contentValues: ContentValues?, extras: Bundle?): Uri? {
        when (uriMatcher.match(uri)) {
            CREATE_BOOK -> {
                val title = contentValues?.getAsString("title") ?: ""
                val author = contentValues?.getAsString("author") ?: ""
                val description = contentValues?.getAsString("description") ?: ""
                val rating = contentValues?.getAsFloat("rating") ?: 0.0f
                val popularity = contentValues?.getAsFloat("popularity") ?: 0.0f
                val genres = contentValues?.getAsString("genres") ?: ""
                val tags = contentValues?.getAsString("tags") ?: ""
                val path = contentValues?.getAsString("path") ?: ""
                val fileExtension = contentValues?.getAsString("fileExtension") ?: ""
                val bookItem = BookItem(
                    title = title,
                    author = author,
                    description = description,
                    rating = rating,
                    popularity = popularity,
                    genres = Genres.valueOf(genres),
                    tags = tags,
                    path = path,
                    fileExtension = fileExtension,
                    startOnPage = 0
                )
                CoroutineScope(Dispatchers.IO).launch {
                    addToMyBookListUseCase.addToMyBookList(bookItem)
                }
            }
        }
        return super.insert(uri, contentValues, extras)
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            GET_BOOK_BY_ID -> {
                var cursor: Cursor? = null
                try {
                    val contentUri = ContentUris.parseId(uri)
                    cursor = bookListDao.getBookItemCursor(contentUri.toInt())
                } catch (e: Exception) {
                    Log.d("TEST_WORKER", e.localizedMessage)
                }
                return cursor
            }
            GET_SHARED_BOOKS -> {
                var cursor: Cursor? = null
                try {
                    cursor = bookListDao.getSharedBooks()
                } catch (e: Exception) {
                    Log.d("TEST_WORKER", e.localizedMessage)
                }
                return cursor
            }
            else -> {
                null
            }
        }
    }

    override fun getType(p0: Uri): String? {
        return null
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        return null
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        return -1
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        return -1
    }

    companion object {
        private const val AUTHORITY = "com.skrash.book"
        private const val GET_BOOK_BY_ID = 101
        private const val GET_SHARED_BOOKS = 102
        private const val CREATE_BOOK = 103
    }
}