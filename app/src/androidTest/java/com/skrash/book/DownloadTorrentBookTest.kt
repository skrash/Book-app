package com.skrash.book

import android.os.Environment
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.google.gson.Gson
import com.skrash.book.data.network.model.BookItemDto
import org.junit.Assert.*
import com.skrash.book.service.DownloadBookWorker
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class DownloadTorrentBookTest {



    @Test
    fun downloadTorrentBook() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val dataPath =
            appContext.getExternalFilesDir(Environment.getDataDirectory().absolutePath)?.absolutePath
        for (i in File(dataPath).listFiles()){
            i.delete()
        }
        val book = BookItemDto(
            author = "",
            description = "овоа",
            fileExtension = "pdf",
            genres = "Other",
            popularity = 0.0,
            rating = 0.0,
            tags = "вов",
            title = "d_107171776"
        )
        val gson = Gson()
        val bookJson = gson.toJson(book)
        val downloadWorker = WorkManager.getInstance(appContext.applicationContext)
        downloadWorker.enqueueUniqueWork(
            DownloadBookWorker.WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            DownloadBookWorker.makeRequest(bookJson)
        )
        Thread.sleep(15000)
        val tfile = File("$dataPath${book.title}.torrent")
        for (i in File(dataPath).listFiles()){
            println("files in data: ${i.name}")
        }
        assertFalse(tfile.exists())
        println("torrent file lenght: ${tfile.length()}")
        assertEquals(tfile.length(), 2999076)
    }
}