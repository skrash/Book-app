package com.skrash.book.data.myBook

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MyBookListDao {

    @Query("SELECT * FROM book_items")
    fun getMyBookList(): LiveData<List<MyBookItemDbModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMyBookItem(myBookItemDbModel: MyBookItemDbModel)

    @Query("DELETE FROM book_items WHERE id=:bookItemId")
    suspend fun deleteMyBookItem(bookItemId: Int)

    @Query("SELECT * FROM book_items WHERE id=:bookItemID LIMIT 1")
    suspend fun getBookItem(bookItemID: Int): MyBookItemDbModel
}