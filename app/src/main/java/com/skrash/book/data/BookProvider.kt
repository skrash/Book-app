package com.skrash.book.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.skrash.book.data.myBook.MyBookListDao
import com.skrash.book.BookApplication
import javax.inject.Inject

class BookProvider : ContentProvider() {

    private val component by lazy {
        (context as BookApplication).component
    }

    @Inject
    lateinit var bookListDao: MyBookListDao

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, "book/#", GET_BOOK_BY_ID  )
        addURI(AUTHORITY, "shared", GET_SHARED_BOOKS  )
    }

    override fun onCreate(): Boolean {
        component.inject(this)
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            GET_BOOK_BY_ID -> {
                var cursor:Cursor? = null
                try {
                    val contentUri = ContentUris.parseId(uri)
                    cursor = bookListDao.getBookItemCursor(contentUri.toInt())
                } catch (e: Exception){
                    Log.d("TEST_WORKER", e.localizedMessage)
                }
                return cursor
            }
            GET_SHARED_BOOKS -> {
                var cursor:Cursor? = null
                try {
                    cursor = bookListDao.getSharedBooks()
                } catch (e: Exception){
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
    }
}