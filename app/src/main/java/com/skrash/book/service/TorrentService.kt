package com.skrash.book.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.skrash.book.R
import com.skrash.book.data.network.ApiFactory
import com.turn.ttorrent.client.Client
import com.turn.ttorrent.client.ConnectionHandler
import com.turn.ttorrent.client.SharedTorrent
import com.turn.ttorrent.common.Torrent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.channels.FileChannel


class TorrentService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Title")
        .setContentText("Text")
        .setSmallIcon(R.drawable.ic_launcher_background)
        .build()


    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val address = InetAddress.getByName("0.0.0.0")
                    val dataPath = getExternalFilesDir(Environment.getDataDirectory().absolutePath)
                    ?.absolutePath
                val sharedBookCursor: Cursor? = contentResolver.query(
                    Uri.parse("content://com.skrash.book/shared"),
                    null,
                    null,
                    null,
                    null,
                    null
                )
                while (sharedBookCursor?.moveToNext() == true) {
                    val title = sharedBookCursor.getString(sharedBookCursor.getColumnIndexOrThrow("title"))
                    val torrentFile = File("$dataPath/$title.torrent")
                    val torrent = Torrent.load(torrentFile)
                    val sharedFile = File(dataPath)
                    val sharedTorrent = SharedTorrent(torrent, sharedFile)
                    val client = Client(address, sharedTorrent)
                    client.share()
                    Thread{
                        Thread.sleep(3000)
                        Log.d("TEST_WORKER", "client state: ${client.state}")
                        Log.d("TEST_WORKER", "client port: ${client.peerSpec.port}")

                    }.start()
                    ApiFactory.apiService.announce(torrent.hexInfoHash, client.peerSpec.hexPeerId, 1, 0, 1, client.peerSpec.port)
                }
            } catch (e: Exception) {
                Log.d("TEST_WORKER", "torrent client: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
        return START_STICKY
    }

    companion object {

        private const val CHANNEL_ID = "channel_id"
        private const val CHANNEL_NAME = "channel_name"
        private const val NOTIFICATION_ID = 1

        fun newIntent(context: Context): Intent {
            return Intent(context, TorrentService::class.java)
        }
    }
}