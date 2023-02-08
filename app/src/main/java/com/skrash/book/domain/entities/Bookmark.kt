package com.skrash.book.domain.entities

data class Bookmark(
    val bookmarkID: Int = UNDEFINED_ID,
    val bookID: Int,
    val page: Int
){
    companion object {
        const val UNDEFINED_ID = 0
    }
}
