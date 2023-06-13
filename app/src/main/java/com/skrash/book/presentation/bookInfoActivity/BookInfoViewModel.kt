package com.skrash.book.presentation.bookInfoActivity

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.Genres
import com.skrash.book.domain.usecases.MyList.GetAllMyBookHashes
import com.skrash.book.domain.usecases.MyList.GetBookCoverUseCase
import com.skrash.book.domain.usecases.MyList.GetMyBookItemByHash
import com.skrash.book.domain.usecases.MyList.GetMyBookUseCase
import com.skrash.book.domain.usecases.VoteUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ConnectException
import javax.inject.Inject

class BookInfoViewModel @Inject constructor(
    private val getMyBookUseCase: GetMyBookUseCase,
    private val getBookCoverUseCase: GetBookCoverUseCase,
    private val getAllMyBookHashes: GetAllMyBookHashes,
    private val getMyBookItemByHash: GetMyBookItemByHash,
    private val voteUseCase: VoteUseCase
) : ViewModel() {
    private val _bookItem = MutableLiveData<BookItem>()
    val bookItem: LiveData<BookItem>
        get() = _bookItem

    private val _imgCover = MutableLiveData<Bitmap>()
    val imgCover
        get() = _imgCover

    private val _isDownloading = MutableLiveData<Boolean>(false)
    val isDownloading: LiveData<Boolean>
        get() = _isDownloading

    private val _downloadingProgress = MutableLiveData<Int>(0)
    val downloadingProgress: LiveData<Int>
        get() = _downloadingProgress

    private var _workerLiveData: LiveData<WorkInfo>? = null
    val workerLiveData: LiveData<WorkInfo>?
        get() = _workerLiveData

    fun getBookItem(bookItemId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val item = getMyBookUseCase.getMyBook(bookItemId)
            _bookItem.postValue(item)
            val itemCover = getBookCoverUseCase.getBookCover(
                BookItem(
                    id = -1,
                    title = "",
                    author = "",
                    description = "",
                    rating = 0.0f,
                    popularity = 0.0f,
                    genres = Genres.Other,
                    tags = "",
                    path = item.path,
                    startOnPage = 0,
                    fileExtension = item.fileExtension.uppercase(),
                    hash = ""
                ),
                300,
                500
            )
            _imgCover.postValue(itemCover)
        }
    }

    fun setBookItemByHash(hash: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _bookItem.postValue(getMyBookItemByHash.getMyBookItemByHash(hash))
        }
    }

    suspend fun checkNetBookIsMyBook(): Boolean {
        val hashesList = getAllMyBookHashes.getAllMyBookHashes()
        for (hashMyBook in hashesList) {
            if (_bookItem.value!!.hash == hashMyBook) {
                return true
            }
        }
        return false
    }

    fun setNetBook(
        title: String,
        description: String,
        author: String,
        genres: String,
        tags: String,
        popularity: Float,
        rating: Float,
        hash: String
    ) {
        _bookItem.value = BookItem(
            title = title,
            author = author,
            description = description,
            genres = Genres.valueOf(genres),
            tags = tags,
            popularity = popularity,
            rating = rating,
            fileExtension = "",
            path = "network",
            startOnPage = -1,
            hash = hash
        )
    }

    fun setDownloading(isDownloading: Boolean) {
        _isDownloading.value = isDownloading
    }

    fun setDownloadingProgress(progress: Int) {
        _downloadingProgress.value = progress
    }

    fun setWorkerLiveData(workerLiveDataSrc: LiveData<WorkInfo>) {
        _workerLiveData = workerLiveDataSrc
    }

    fun vote(id: String, votePoint: Int, errorCallback: () -> Unit) {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            if (throwable is ConnectException) {
                CoroutineScope(Dispatchers.Main).launch {
                    errorCallback.invoke()
                }
            }
            Log.d("ERRORS", throwable.localizedMessage)
            throwable.printStackTrace()
        }
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            voteUseCase.vote(bookItem.value!!, id, votePoint)
        }
    }
}