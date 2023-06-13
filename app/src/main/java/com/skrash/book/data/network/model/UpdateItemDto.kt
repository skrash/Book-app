package com.skrash.book.data.network.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateItemDto(

    @SerializedName("hash")
    @Expose
    val hash: String,

    @SerializedName("rating")
    @Expose
    val rating: Int,

    @SerializedName("popularity")
    @Expose
    val popularity: Int,

    @SerializedName("voted")
    @Expose
    val voted: Boolean
) : Parcelable