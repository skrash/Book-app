package com.skrash.book.service

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.work.*
import com.skrash.book.data.TorrentSettings
import com.skrash.book.data.network.ApiFactory
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.Genres
import com.turn.ttorrent.common.Torrent
import com.turn.ttorrent.tracker.Tracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import java.io.File
import java.io.FileOutputStream
import java.lang.RuntimeException
import java.net.InetAddress
import java.net.InetSocketAddress


class SendTrackerWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val id = workerParameters.inputData.getInt(ID, -1)
        try {
            val bookCursor: Cursor? = context.contentResolver.query(
                Uri.parse("content://com.skrash.book/book/$id"),
                null,
                null,
                null,
                null,
                null
            )
            while (bookCursor?.moveToNext() == true) {
                val title = bookCursor.getString(bookCursor.getColumnIndexOrThrow("title"))
                val author = bookCursor.getString(bookCursor.getColumnIndexOrThrow("author"))
                val description =
                    bookCursor.getString(bookCursor.getColumnIndexOrThrow("description"))
                val rating = bookCursor.getFloat(bookCursor.getColumnIndexOrThrow("rating"))
                val popularity = bookCursor.getFloat(bookCursor.getColumnIndexOrThrow("popularity"))
                val genres = bookCursor.getString(bookCursor.getColumnIndexOrThrow("genres"))
                val tags = bookCursor.getString(bookCursor.getColumnIndexOrThrow("tags"))
                val path = bookCursor.getString(bookCursor.getColumnIndexOrThrow("path"))
                val fileException =
                    bookCursor.getString(bookCursor.getColumnIndexOrThrow("fileExtension"))
                val startOnPage = bookCursor.getInt(bookCursor.getColumnIndexOrThrow("startOnPage"))
                val shareAccess =
                    bookCursor.getInt(bookCursor.getColumnIndexOrThrow("shareAccess")) > 0
                val id = bookCursor.getInt(bookCursor.getColumnIndexOrThrow("id"))
                val book = BookItem(
                    title,
                    author,
                    description,
                    rating,
                    popularity,
                    Genres.valueOf(genres),
                    tags,
                    path,
                    fileException,
                    startOnPage,
                    shareAccess,
                    id
                )
                bookCursor.close()
                publish(book)
            }
        } catch (e: Exception) {
            Log.d("TEST_WORKER", e.localizedMessage)
            e.printStackTrace()
            Log.d("TEST_WORKER", "Cause = ${e.cause}")
        }
        return Result.success()
    }

    private fun publish(book: BookItem) {
        val socketAddress =
            InetSocketAddress(TorrentSettings.DEST_ADDRESS, TorrentSettings.DEST_PORT)
        val tracker = Tracker(socketAddress)
        tracker.start()
        val file = File(
            Uri.parse(book.path).path
                ?: throw RuntimeException("unable to generate uri path. perhaps the path is wrong")
        )
        val torrentFile = Torrent.create(file, tracker.announceUrl.toURI(), "")
        val dataPath = context.getExternalFilesDir(Environment.getDataDirectory().absolutePath)
            ?.absolutePath
        val pathStr = dataPath + "/" + book.title + ".torrent"
        val fos = FileOutputStream(pathStr)
        torrentFile.save(fos)
        fos.close()
        var result =
            ApiFactory.apiService.publishTorrent(torrentFile.hexInfoHash, 1, 5000, 100, 1, 1)
        CoroutineScope(Dispatchers.IO).launch {
            result.awaitResponse().body()?.let { Log.d("TEST_WORKER", it.string()) }
        }
    }

    companion object {

        const val WORK_NAME = "BTWorker"
        const val ID = "id"

        fun makeRequest(id: Int): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<SendTrackerWorker>()
                .setInputData(workDataOf(ID to id))
                .setConstraints(makeConstraints())
                .build()
        }

        private fun makeConstraints(): Constraints {
            return Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        }

    }

}