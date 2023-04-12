package com.skrash.book.data.network.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookItemDto(
    @SerializedName("author")
    @Expose
    var author: String? = null,

    @SerializedName("description")
    @Expose
    var description: String? = null,

    @SerializedName("genres")
    @Expose
    var genres: String? = null,

    @SerializedName("popularity")
    @Expose
    var popularity: Double? = null,

    @SerializedName("rating")
    @Expose
    var rating: Double? = null,

    @SerializedName("tags")
    @Expose
    var tags: String? = null,

    @SerializedName("title")
    @Expose
    var title: String? = null
): Parcelable
