package com.skrash.book.torrent

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.work.*
import com.google.gson.Gson
import com.skrash.book.data.TorrentSettings
import com.skrash.book.data.network.ApiFactory
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.Genres
import com.skrash.book.torrent.client.common.TorrentCreator
import com.skrash.book.torrent.client.common.TorrentSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.net.URI


class UploadTorrentFileWorker(
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
                val hash = bookCursor.getString(bookCursor.getColumnIndexOrThrow("hash"))
                val book = BookItem(
                    title = title,
                    author = author,
                    description = description,
                    rating = rating,
                    popularity = popularity,
                    genres = Genres.valueOf(genres),
                    tags = tags,
                    path = path,
                    fileExtension = fileException,
                    startOnPage = startOnPage,
                    shareAccess = shareAccess,
                    hash = hash,
                    id = id
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
        val tfile = createTorrentFile(book)
        val torrentFile = File(tfile)
        val requestFile =
            RequestBody.create(MediaType.parse("application/x-bittorrent"), torrentFile)
        val gson = Gson()
        val bookJson = gson.toJson(book)
        val bookInfoPart = RequestBody.create(MediaType.parse("text/plain"), bookJson)
        val filePart = MultipartBody.Part.createFormData("file", book.title, requestFile)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiFactory.apiService.publishTorrent(filePart, bookInfoPart)
            } catch (e: Exception) {
                Log.d("TEST_WORKER", e.localizedMessage)
            }
        }
    }

    private fun createTorrentFile(book: BookItem): String {
        val bookFile = File(
            Uri.parse(book.path).path
                ?: throw RuntimeException("unable to generate uri path. perhaps the path is wrong")
        )
        val dataPath = context.getExternalFilesDir(Environment.getDataDirectory().absolutePath)
            ?.absolutePath
        val torrentFilePath = dataPath + "/" + book.title + ".torrent"
        val fos = FileOutputStream(torrentFilePath)
        val metadata = TorrentCreator.create(bookFile, URI.create("http://${TorrentSettings.DEST_ADDRESS}:${TorrentSettings.DEST_PORT}/announce"), "")
        fos.write(TorrentSerializer().serialize(metadata))
        fos.close()
        return torrentFilePath
    }

    companion object {

        const val WORK_NAME = "BTWorker"
        const val ID = "id"

        fun makeRequest(id: Int): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<UploadTorrentFileWorker>()
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