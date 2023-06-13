package com.skrash.book.data.Bookmark

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmarkItem(bookMarkDbModel: BookMarkDbModel)

    @Query("DELETE FROM bookmark WHERE bookID=:bookId AND page=:pageNum")
    suspend fun deleteBookmarkItem(bookId: Int, pageNum: Int)

    @Query("SELECT * FROM bookmark WHERE bookID=:bookmarks")
    fun getBookmarkList(bookmarks: Int): LiveData<List<BookMarkDbModel>>
}