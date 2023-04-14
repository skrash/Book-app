package com.skrash.book.presentation.bookInfoActivity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.google.gson.Gson
import com.skrash.book.R
import com.skrash.book.databinding.FragmentBookInfoBinding
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.BookApplication
import com.skrash.book.data.network.model.BookItemDto
import com.skrash.book.presentation.ViewModelFactory
import com.skrash.book.presentation.openBookActivity.OpenBookActivity
import com.skrash.book.service.DownloadBookWorker
import com.skrash.book.service.SendTrackerWorker
import javax.inject.Inject

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

    private fun initCover(){
        viewModel.imgCover.observe(viewLifecycleOwner){
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
        if (args.getString(MODE) == MODE_MY_BOOK){
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
        if(!modeIsMyBook){
            binding.btnOpen.text = getString(R.string.download)
        }
        viewModel = ViewModelProvider(this, viewModelFactory)[BookInfoViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        if (requireArguments().getString(MODE) == MODE_NET_BOOK){
            viewModel.setNetBook(title = bookItemDto?.title ?: "",
                author = bookItemDto?.author ?: "",
                description = bookItemDto?.description ?: "",
                tags = bookItemDto?.tags ?: "",
                genres = bookItemDto?.genres ?: throw java.lang.RuntimeException("incorrect value tags"),
                popularity = bookItemDto?.popularity?.toFloat() ?: 0f,
                rating = bookItemDto?.rating?.toFloat() ?: 0f
            )
        }
        if (modeIsMyBook) {
            viewModel.getBookItem(bookItemId)
        }
        binding.btnOpen.setOnClickListener {
            if (modeIsMyBook){
                if(viewModel.bookItem.value != null){
                    startActivity(OpenBookActivity.newIntent(requireContext(), viewModel.bookItem.value!!.id))
                }
            } else {
                val gson = Gson()
                val bookJson = gson.toJson(bookItemDto)
                val downloadWorker = WorkManager.getInstance(requireContext().applicationContext)
                downloadWorker.enqueueUniqueWork(
                    DownloadBookWorker.WORK_NAME,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    DownloadBookWorker.makeRequest(bookJson)
                )
            }
        }
        initCover()
        viewModel.bookItem.observe(viewLifecycleOwner){
            if (it != null){
                if (requireArguments().getString(MODE) == MODE_MY_BOOK){
                    if (it.shareAccess){
                        binding.ivShareAccess.setImageResource(R.drawable.ic_baseline_cloud_upload_24_green)
                    } else {
                        binding.ivShareAccess.setImageResource(R.drawable.ic_baseline_cloud_upload_24)
                    }
                    binding.ivShareAccess.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {

        private const val BOOK_ITEM_ID = "book_item_id"
        private const val MODE = "mode"
        private const val BookItemDto = "bookItemDto"
        private const val MODE_MY_BOOK = "my_book"
        private const val MODE_NET_BOOK = "net_book"

        fun newInstanceOpenItem(bookItemId: Int, mode: String, bookItemDto: BookItemDto? = null): BookInfoFragment {
            return BookInfoFragment().apply {
                arguments = Bundle().apply {
                    putInt(BOOK_ITEM_ID, bookItemId)
                    if (mode == MODE_MY_BOOK){
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