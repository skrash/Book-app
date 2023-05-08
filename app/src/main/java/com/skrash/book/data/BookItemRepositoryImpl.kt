package com.skrash.book.data

import android.provider.Settings
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.skrash.book.data.network.ApiFactory
import com.skrash.book.data.network.model.BookItemDto
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.usecases.BookItemRepository
import retrofit2.Call
import java.security.MessageDigest
import javax.inject.Inject

class BookItemRepositoryImpl @Inject constructor(): BookItemRepository {

    override suspend fun getBookItemList(): Call<List<BookItemDto>> {
        return ApiFactory.apiService.listBook()
    }

    override suspend fun vote(bookItem: BookItem, id: String, votePoint: Int) {
        val hashedID = getHexDigestSha1(id)
        val shuffledHashedID = shuffleAlgorithm(hashedID)
        ApiFactory.apiService.vote(bookItem.hash, shuffledHashedID, votePoint)
    }

    private fun getHexDigestSha1(id: String): String {
        val sha1 = MessageDigest.getInstance("SHA-256")
        val sha1hash = sha1.digest(id.toByteArray(Charsets.UTF_8))
        val builder = StringBuilder()
        for (b in sha1hash) {
            builder.append(String.format("%02x", b))
        }
        return id + builder
    }

    private fun shuffleAlgorithm(inStr: String): String {
        if (inStr.length != 80) throw RuntimeException("incorrect length")
        var result = ""
        val arrayChunk = inStr.chunked(10)
        for (chunk in arrayChunk) {
            val newChunk = charArrayOf(
                chunk[1], chunk[9], chunk[0], chunk[3], chunk[2], chunk[8], chunk[7], chunk[6], chunk[4], chunk[5]
            )
            result += String(newChunk)
        }
        return result
    }

}