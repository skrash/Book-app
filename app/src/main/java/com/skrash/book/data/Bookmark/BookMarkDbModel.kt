package com.skrash.book.data.Bookmark

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.skrash.book.data.myBook.MyBookItemDbModel

@Entity(tableName = "bookmark", foreignKeys = [ForeignKey(
    entity = MyBookItemDbModel::class,
    parentColumns = arrayOf("id"),
    childColumns = arrayOf("bookID"),
    onDelete = ForeignKey.CASCADE
)])
class BookMarkDbModel(
    @PrimaryKey(autoGenerate = true)
    val bookmarkID: Int,
    val bookID: Int,
    val page: Int,
    val comment: String
)