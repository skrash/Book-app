package com.skrash.book.data.network.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RequestUpdateDto (
    val userID: String,
    val listBookHashes: List<String>
    ): Parcelable