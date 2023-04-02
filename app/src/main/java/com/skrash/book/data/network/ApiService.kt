package com.skrash.book.data.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("announce")
    fun publishTorrent(
        @Query("info_hash") info_hash: String,
        @Query("peer_id") peer_id: Int,
        @Query("port") port: Int,
        @Query("uploaded") uploaded: Int,
        @Query("downloaded") downloaded: Int,
        @Query("left") left: Int
    ): Call<ResponseBody>
}