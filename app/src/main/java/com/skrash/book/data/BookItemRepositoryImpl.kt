package com.skrash.book.data

import android.provider.Settings
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.skrash.book.data.network.ApiFactory
import com.skrash.book.data.network.EncryptIDAlgorithm.Companion.getHexDigestSha1
import com.skrash.book.data.network.EncryptIDAlgorithm.Companion.shuffleAlgorithm
import com.skrash.book.data.network.model.BookItemDto
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.usecases.BookItemRepository
import retrofit2.Call
import java.security.MessageDigest
import javax.inject.Inject

class BookItemRepositoryImpl @Inject constructor(): BookItemRepository {

    override suspend fun getBookItemList(userID: String): Call<List<BookItemDto>> {
        val hashedID = getHexDigestSha1(userID)
        val shuffledHashedID = shuffleAlgorithm(hashedID)
        return ApiFactory.apiService.listBook(shuffledHashedID)
    }

    override suspend fun vote(bookItem: BookItem, id: String, votePoint: Int) {
        val hashedID = getHexDigestSha1(id)
        val shuffledHashedID = shuffleAlgorithm(hashedID)
        ApiFactory.apiService.vote(bookItem.hash, shuffledHashedID, votePoint)
    }
}