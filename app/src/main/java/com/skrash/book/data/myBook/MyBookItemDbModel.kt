package com.skrash.book.data.myBook

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skrash.book.domain.entities.Genres

@Entity(tableName = "book_items")
data class MyBookItemDbModel
    (
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    val title: String,
    val author: String,
    val description: String,
    val rating: Float,
    val popularity: Float,
    val genres: Genres,
    val tags: String,
    val path: String,
    val startOnPage: Int,
    val fileExtension: String
)