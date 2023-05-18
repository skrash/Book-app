package com.skrash.book.presentation.mainAcitivity

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.RecyclerView
import com.skrash.book.BookApplication
import com.skrash.book.R
import com.skrash.book.data.network.model.BookItemDtoMapper
import com.skrash.book.databinding.ActivityMainBinding
import com.skrash.book.databinding.BookItemBinding
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import com.skrash.book.domain.entities.Genres
import com.skrash.book.presentation.RequestFileAccess
import com.skrash.book.presentation.ViewModelFactory
import com.skrash.book.presentation.YandexID
import com.skrash.book.presentation.addBookActivity.AddBookActivity
import com.skrash.book.presentation.addBookActivity.AddBookItemFragment
import com.skrash.book.presentation.bookInfoActivity.BookInfoActivity
import com.skrash.book.presentation.bookInfoActivity.BookInfoFragment
import com.skrash.book.torrent.ShareBookService
import com.yandex.mobile.ads.banner.AdSize
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject


class MainActivity : AppCompatActivity(), AddBookItemFragment.OnEditingFinishedListener,
    OnRequestPermissionsResultCallback {

    private lateinit var binding: ActivityMainBinding
    private var bookListAdapter: BookListAdapter? = null
    private lateinit var viewModel: MainActivityViewModel
    private var itemTouchHelper: ItemTouchHelper? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (application as BookApplication).component
    }
    private lateinit var intent: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainActivityViewModel::class.java]
        setContentView(binding.root)
        viewModel.updateDB(
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ANDROID_ID
            )
        )
        loadAd()
        intent = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
            val dataPath =
                getExternalFilesDir(Environment.getDataDirectory().absolutePath)?.absolutePath
                    ?: throw RuntimeException("failed to create path to data directory")
            for (uri in it) {
                val fileExtension = uri.path?.split(".")?.last()
                    ?: throw RuntimeException("failed get file extension from uri")
                val cur = contentResolver.query(uri, null, null, null)
                var fileName = ""
                if (cur != null) {
                    cur.use { cur ->
                        if (cur.moveToFirst()) {
                            fileName = cur.getString(0)
                        }
                    }
                    fileName = fileName.split("/").last()
                    val exceptionHandler =
                        CoroutineExceptionHandler { _, throwable ->
                            if (throwable is IllegalArgumentException) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(
                                        this@MainActivity,
                                        getString(R.string.incorrect_extension),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                        val openStream: InputStream = contentResolver.openInputStream(uri)
                            ?: throw RuntimeException("failed get output stream from file")
                        val dataFile = File("$dataPath/$fileName")
                        if (!dataFile.exists()) {
                            dataFile.createNewFile()
                        }
                        val fileOutputStream = FileOutputStream(dataFile)
                        fileOutputStream.write(openStream.readBytes())
                        openStream.close()
                        fileOutputStream.close()
                        viewModel.compileDefaultBookItem(
                            dataFile.absolutePath,
                            FormatBook.valueOf(fileExtension.uppercase())
                        )

                    }
                }
            }
        }
        checkFirstRun()
        val context = this
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.initMyBook(Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ANDROID_ID
            )) {
                Toast.makeText(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
            }
        }
        if (viewModel.mode == "" || viewModel.mode == MODE_MY_BOOK) {
            viewMyBook()
        } else {
            viewNetBook()
        }
        setupRecyclerView()
        ContextCompat.startForegroundService(
            this,
            ShareBookService.newIntent(this)
        )
    }

    private fun loadAd() {
        binding.yaBanner.setAdUnitId(YandexID.AdUnitId)
        binding.yaBanner.setAdSize(AdSize.stickySize(300))
        val adRequest = AdRequest.Builder().build()
        binding.yaBanner.loadAd(adRequest)
    }

    private fun setupOnClickListeners() {

        binding.btnAdd.setOnClickListener {
            if (viewModel.mode == "" || viewModel.mode == MODE_MY_BOOK) {
                if (binding.fragmentContainer == null) {
                    val intent = AddBookActivity.newIntentAddBook(this)
                    startActivity(intent)
                } else {
                    launchFragment(AddBookItemFragment.newInstanceAddItem())
                }
            }
        }
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.title) {
                getString(R.string.general_book) -> {
                    binding.btnAdd.visibility = View.GONE
                    viewModel.mode = MODE_NET_BOOK
                    viewNetBook()
                    true
                }
                getString(R.string.my_book) -> {
                    binding.btnAdd.visibility = View.VISIBLE
                    viewModel.mode = MODE_MY_BOOK
                    viewMyBook()
                    true
                }
                else -> false
            }
        }
    }

    private fun viewMyBook() {
        viewModel.mode = MODE_MY_BOOK
        binding.btnAdd.visibility = View.VISIBLE
        setupSwipeListener(binding.mainRecycler)
        viewModel.netBookList.observe(this) {

        }
        viewModel.bookList.observe(this) {
            bookListAdapter?.submitList(it)
        }
        setupClickListener()
    }

    private fun viewNetBook() {
        viewModel.mode = MODE_NET_BOOK
        itemTouchHelper?.attachToRecyclerView(null)
        disableListeners()
        binding.btnAdd.visibility = View.GONE
        viewModel.netBookList.observe(this) {
            bookListAdapter?.submitList(it)
        }
        viewModel.bookList.observe(this) {

        }
    }

    private fun disableListeners() {
        bookListAdapter?.loadCoverFunction = { bookItemViewHolder, bookItem -> }
        bookListAdapter?.onEditBookClickListener = {}
    }

    private fun requestDialogChangeFilesFirstRun() {
        intent.launch("*/*")
    }

    private fun initBookList() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.first_run_change_books_alert_title))
            .setMessage(getString(R.string.first_run_change_books_alert_body))
            .setPositiveButton(
                android.R.string.ok
            ) { _, _ ->
                requestDialogChangeFilesFirstRun()
            }
            .show()
    }

    private fun checkFirstRun() {
        val pref = getSharedPreferences(packageName, MODE_PRIVATE)
        if (pref.getBoolean("first_run", true)) {
            initBookList()
            pref.edit().putBoolean("first_run", false).apply()
        }
    }

    private fun setupRecyclerView() {
        with(binding.mainRecycler) {
            bookListAdapter = BookListAdapter()
            adapter = bookListAdapter
        }
        setupClickListener()
        setupSwipeListener(binding.mainRecycler)
        setupOnClickListeners()
    }

    private fun setupSwipeListener(rvShopList: RecyclerView) {
        if (viewModel.mode == MODE_MY_BOOK) {
            val callback = object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {

                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    val dragFlags = if (viewModel.mode == MODE_MY_BOOK) START else 0
                    return makeMovementFlags(dragFlags, dragFlags)
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val item = bookListAdapter?.currentList?.get(viewHolder.adapterPosition)
                    if (item != null) {
                        viewModel.deleteShopItem(item)
                    }
                }
            }
            itemTouchHelper = ItemTouchHelper(callback)
            itemTouchHelper!!.attachToRecyclerView(rvShopList)
        }
    }

    private fun setupClickListener() {
        if (viewModel.mode == "" || viewModel.mode == MODE_MY_BOOK) {
            bookListAdapter?.disableUnused = { bookItem, binding ->
                if (viewModel.mode == MODE_MY_BOOK) {
                    if (bookItem.shareAccess) {
                        binding.ivShareAccess.setImageResource(R.drawable.ic_baseline_cloud_upload_24_green)
                    } else {
                        binding.ivShareAccess.setImageResource(R.drawable.ic_baseline_cloud_upload_24)
                    }
                } else {
                    binding.btnEdit.visibility = View.GONE
                }
            }
            bookListAdapter?.onEditBookClickListener = {
                if (binding.fragmentContainer == null) {
                    val intent = AddBookActivity.newIntentEditBook(this, it.id)
                    startActivity(intent)
                } else {
                    launchFragment(AddBookItemFragment.newInstanceEditItem(it.id))
                }
            }
            bookListAdapter?.onBookItemClickListener = {
                if (viewModel.mode == MODE_MY_BOOK) {
                    if (binding.fragmentContainer == null) // check landscape orientation
                    {
                        val intent = BookInfoActivity.newIntentOpenItem(
                            this,
                            it.id,
                            BookInfoActivity.MODE_MY_BOOK
                        )
                        startActivity(intent)
                    } else {
                        launchFragment(
                            BookInfoFragment.newInstanceOpenItem(
                                it.id,
                                BookInfoActivity.MODE_MY_BOOK
                            )
                        )
                    }
                } else {
                    if (binding.fragmentContainer == null) // check landscape orientation
                    {
                        val intent = BookInfoActivity.newIntentOpenItem(
                            this,
                            BookItem.UNDEFINED_ID,
                            BookInfoActivity.MODE_NET_BOOK,
                            BookItemDtoMapper.bookItemToBookItemDto(it)
                        )
                        startActivity(intent)
                    } else {
                        launchFragment(
                            BookInfoFragment.newInstanceOpenItem(
                                BookItem.UNDEFINED_ID,
                                BookInfoActivity.MODE_NET_BOOK,
                                BookItemDtoMapper.bookItemToBookItemDto(it)
                            )
                        )
                    }
                }
            }
            RequestFileAccess.requestFileAccessPermission(this, {
                bookListAdapter?.loadCoverFunction = { holder, itemBook ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val bitmap = viewModel.getBookCover(
                            BookItem(
                                id = -1,
                                title = "",
                                author = "",
                                description = "",
                                rating = 0.0f,
                                popularity = 0.0f,
                                genres = Genres.Other,
                                tags = "",
                                path = itemBook.path,
                                startOnPage = 0,
                                fileExtension = itemBook.path.substringAfterLast(".", "")
                                    .uppercase(),
                                hash = itemBook.hash
                            ),
                            150,
                            150
                        )
                        withContext(Dispatchers.Main) {
                            val bindingCover = holder.binding as BookItemBinding
                            bindingCover.imCover.setImageBitmap(bitmap)
                        }
                    }
                }
            }) {}
        }
    }

    private fun launchFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onEditingFinishedListener() {
        supportFragmentManager.popBackStack()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty()) {
            if (requestCode == RequestFileAccess.REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkFirstRun()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        const val MODE_MY_BOOK = "my_book"
        private const val MODE_NET_BOOK = "net_book"
    }
}