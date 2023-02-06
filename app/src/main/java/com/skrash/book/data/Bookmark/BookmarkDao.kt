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

    @Query("DELETE FROM bookmark WHERE id=:bookmarkId")
    suspend fun deleteBookmarkItem(bookmarkId: Int)

    @Query("SELECT * FROM bookmark")
    fun getBookmarkList(): LiveData<List<BookMarkDbModel>>
}