package com.skrash.book.data.Bookmark

import com.skrash.book.domain.entities.Bookmark
import javax.inject.Inject

class BookmarkMapper @Inject constructor() {

    fun mapBookmarkToBookmarkDbModel(bookmark: Bookmark) = BookMarkDbModel(
        bookmarkID = bookmark.bookmarkID,
        bookID = bookmark.bookID,
        page = bookmark.page,
        comment = bookmark.comment
    )

    fun mapBookmarkDbModelToBookmark(bookMarkDbModel: BookMarkDbModel) = Bookmark(
        bookmarkID = bookMarkDbModel.bookmarkID,
        bookID = bookMarkDbModel.bookID,
        page = bookMarkDbModel.page,
        comment = bookMarkDbModel.comment
    )

    fun mapListBookmarkDbModelToListBookmark(list: List<BookMarkDbModel>) = list.map {
        mapBookmarkDbModelToBookmark(it)
    }
}