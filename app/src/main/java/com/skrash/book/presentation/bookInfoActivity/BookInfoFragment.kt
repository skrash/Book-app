package com.skrash.book.presentation.bookInfoActivity

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.google.gson.Gson
import com.skrash.book.BookApplication
import com.skrash.book.R
import com.skrash.book.data.network.EncryptIDAlgorithm
import com.skrash.book.data.network.model.BookItemDto
import com.skrash.book.databinding.FragmentBookInfoBinding
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import com.skrash.book.presentation.ViewModelFactory
import com.skrash.book.presentation.YandexID
import com.skrash.book.presentation.openBookActivity.fb2Activity.OpenFB2BookActivity
import com.skrash.book.presentation.openBookActivity.pdfActivity.OpenBookActivity
import com.skrash.book.torrent.DownloadBookWorker
import com.yandex.mobile.ads.banner.AdSize
import com.yandex.mobile.ads.common.AdRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.ConnectException
import javax.inject.Inject
import kotlin.math.absoluteValue

class BookInfoFragment : Fragment() {

    private lateinit var viewModel: BookInfoViewModel

    private var _binding: FragmentBookInfoBinding? = null
    private val binding: FragmentBookInfoBinding
        get() = _binding ?: throw RuntimeException("FragmentBookInfoBinding is null")

    private var bookItemId: Int = BookItem.UNDEFINED_ID
    private var bookItemDto: BookItemDto? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private var modeIsMyBook = true

    private val component by lazy {
        (requireActivity().application as BookApplication).component
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseParams()
    }

    private fun initCover() {
        viewModel.imgCover.observe(viewLifecycleOwner) {
            binding.imCover.setImageBitmap(it)
        }
    }

    private fun parseParams() {
        val args = requireArguments()
        if (!args.containsKey(BOOK_ITEM_ID)) {
            throw RuntimeException("Param book item id is absent")
        }
        if (!args.containsKey(MODE)) {
            throw RuntimeException("Param mode is absent")
        }
        if (args.getString(MODE) == MODE_MY_BOOK) {
            bookItemId = args.getInt(BOOK_ITEM_ID, BookItem.UNDEFINED_ID)
        } else {
            bookItemDto = args.getParcelable(BookItemDto)
            Log.d("TEST_WORKER", "FRAGMENT ${bookItemDto == null}")
            modeIsMyBook = false
        }
    }

    override fun onAttach(context: Context) {
        component.inject(this)

        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (bookItemDto?.voted == true){
            binding.ratingBar.setIsIndicator(true)
        }
        viewModel = ViewModelProvider(this, viewModelFactory)[BookInfoViewModel::class.java]
        initCover()
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        if (requireArguments().getString(MODE) == MODE_NET_BOOK) {
            binding.btnOpen.visibility = View.GONE
            binding.btnDownload.visibility = View.VISIBLE
            viewModel.setNetBook(
                title = bookItemDto?.title ?: "",
                author = bookItemDto?.author ?: "",
                description = bookItemDto?.description ?: "",
                tags = bookItemDto?.tags ?: "",
                genres = bookItemDto?.genres
                    ?: throw java.lang.RuntimeException("incorrect value tags"),
                popularity = bookItemDto?.popularity?.toFloat() ?: 0f,
                rating = bookItemDto?.rating?.toFloat() ?: 0f,
                hash = bookItemDto!!.hash
            )
            CoroutineScope(Dispatchers.IO).launch {
                val isMyBook = viewModel.checkNetBookIsMyBook()
                if (isMyBook){
                    bookIsDownloaded()
                    binding.btnOpen.visibility = View.VISIBLE
                    viewModel.setBookItemByHash(bookItemDto!!.hash)
                }
            }
        }
        if (modeIsMyBook) {
            viewModel.getBookItem(bookItemId)
        }

        if (viewModel.isDownloading.value == true){
            continueDownloadProgress()
        }

        // реклама
        loadAd()

        binding.btnOpen.setOnClickListener {
            openBook()
        }

        binding.ratingBar.rating = viewModel.bookItem.value?.rating ?: 0f

        binding.btnDownload.setOnClickListener {
            downloadBook()
        }
        viewModel.bookItem.observe(viewLifecycleOwner) {
            if (it != null) {
                if (requireArguments().getString(MODE) == MODE_MY_BOOK) {
                    binding.ratingBar.rating = it.rating
                    if (it.shareAccess) {
                        binding.ivShareAccess.setImageResource(R.drawable.ic_baseline_cloud_upload_24_green)
                    } else {
                        binding.ivShareAccess.setImageResource(R.drawable.ic_baseline_cloud_upload_24)
                    }
                    binding.ivShareAccess.visibility = View.VISIBLE
                }
            }
        }
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            binding.ratingBar.setIsIndicator(true)
            val id = Settings.Secure.getString(
                requireContext().contentResolver,
                Settings.Secure.ANDROID_ID
            )
            CoroutineScope(Dispatchers.Main).launch {
                binding.ratingBar.setBackgroundColor(requireContext().getColor(R.color.light_green))
                delay(1500)
                binding.ratingBar.progressTintList = ColorStateList.valueOf(requireContext().getColor(R.color.dark_gray))
                binding.ratingBar.setBackgroundColor(Color.TRANSPARENT)
                binding.ratingBar.rating = viewModel.bookItem.value?.rating ?: 0f
            }
            viewModel.vote(id, Math.ceil(rating.toDouble()).toInt()){
                Toast.makeText(requireContext(), getText(R.string.network_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun continueDownloadProgress(){
        binding.btnDownload.isEnabled = false
        binding.progressDownload.visibility = View.VISIBLE
        viewModel.workerLiveData!!.observe(viewLifecycleOwner){
            viewModel.setDownloadingProgress(it.progress.getInt(DownloadBookWorker.TAG_PROGRESS, 0))
            if (viewModel.downloadingProgress.value == 100) {
                bookIsDownloaded()
            }
            val id = it.progress.getInt(DownloadBookWorker.TAG_CREATED_BOOK_ID, -1)
            if (id != -1){
                binding.btnOpen.visibility = View.VISIBLE
                viewModel.getBookItem(id)
            }
            binding.progressDownload.progress = viewModel.downloadingProgress.value ?: 0
        }
    }

    private fun downloadBook(){
        binding.btnDownload.isEnabled = false
        viewModel.setDownloading(true)
        var userID = Settings.Secure.getString(
            requireContext().contentResolver,
            Settings.Secure.ANDROID_ID
        )
        val hashedID = EncryptIDAlgorithm.getHexDigestSha1(userID)
        val shuffledHashedID = EncryptIDAlgorithm.shuffleAlgorithm(hashedID)
        bookItemDto!!.userID = shuffledHashedID
        val gson = Gson()
        val bookJson = gson.toJson(bookItemDto)
        val downloadWorker = WorkManager.getInstance(requireContext().applicationContext)
        val request = DownloadBookWorker.makeRequest(bookJson)
        downloadWorker.enqueueUniqueWork(
            DownloadBookWorker.WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
        binding.progressDownload.visibility = View.VISIBLE
        val workerLiveData = downloadWorker.getWorkInfoByIdLiveData(request.id)
        viewModel.setWorkerLiveData(workerLiveData)
        workerLiveData.observe(viewLifecycleOwner) {
            viewModel.setDownloadingProgress(it.progress.getInt(DownloadBookWorker.TAG_PROGRESS, 0))
            if (viewModel.downloadingProgress.value == 100) {
                bookIsDownloaded()
            }
            val id = it.progress.getInt(DownloadBookWorker.TAG_CREATED_BOOK_ID, -1)
            if (id != -1){
                binding.btnOpen.visibility = View.VISIBLE
                viewModel.getBookItem(id)
            }
            binding.progressDownload.progress = viewModel.downloadingProgress.value ?: 0
        }
    }

    private fun bookIsDownloaded(){
        binding.progressDownload.visibility = View.GONE
        modeIsMyBook = true
        binding.llRoot.removeView(binding.btnDownload)
        binding.btnDownload.visibility = View.GONE
    }

    private fun openBook() {
        if (viewModel.bookItem.value != null) {
            when (viewModel.bookItem.value!!.fileExtension) {
                FormatBook.FB2.name.lowercase() -> {
                    startActivity(
                        OpenFB2BookActivity.newIntent(
                            requireContext(),
                            viewModel.bookItem.value!!.id
                        )
                    )
                }
                FormatBook.PDF.name.lowercase() -> {
                    startActivity(
                        OpenBookActivity.newIntent(
                            requireContext(),
                            viewModel.bookItem.value!!.id
                        )
                    )
                }
            }
        }
    }

    private fun loadAd() {
        binding.yaBanner.setAdUnitId(YandexID.AdUnitId)
        binding.yaBanner.setAdSize(AdSize.stickySize(300))
        val adRequest = AdRequest.Builder().build()
        binding.yaBanner.loadAd(adRequest)
    }

    companion object {

        private const val BOOK_ITEM_ID = "book_item_id"
        private const val MODE = "mode"
        private const val BookItemDto = "bookItemDto"
        private const val MODE_MY_BOOK = "my_book"
        private const val MODE_NET_BOOK = "net_book"

        fun newInstanceOpenItem(
            bookItemId: Int,
            mode: String,
            bookItemDto: BookItemDto? = null
        ): BookInfoFragment {
            return BookInfoFragment().apply {
                arguments = Bundle().apply {
                    putInt(BOOK_ITEM_ID, bookItemId)
                    if (mode == MODE_MY_BOOK) {
                        putString(MODE, MODE_MY_BOOK)
                    } else {
                        putParcelable(BookItemDto, bookItemDto)
                        putString(MODE, MODE_NET_BOOK)
                    }
                }
            }
        }
    }
}