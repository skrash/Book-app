package com.skrash.book.presentation.mainAcitivity

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.skrash.book.BookApplication
import com.skrash.book.R
import com.skrash.book.databinding.ActivityMainBinding
import com.skrash.book.databinding.BookItemBinding
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import com.skrash.book.domain.entities.Genres
import com.skrash.book.presentation.RequestFileAccess
import com.skrash.book.presentation.ViewModelFactory
import com.skrash.book.presentation.addBookActivity.AddBookActivity
import com.skrash.book.presentation.addBookActivity.AddBookItemFragment
import com.skrash.book.presentation.bookInfoActivity.BookInfoActivity
import com.skrash.book.presentation.bookInfoActivity.BookInfoFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject


class MainActivity : AppCompatActivity(), AddBookItemFragment.OnEditingFinishedListener,
    OnRequestPermissionsResultCallback {

    private lateinit var binding: ActivityMainBinding
    private var bookListAdapter: BookListAdapter? = null
    private lateinit var viewModel: MainActivityViewModel
    private var bookListAdapterNet: BookNetworkAdapter? = null
    private var itemTouchHelper: ItemTouchHelper? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (application as BookApplication).component
    }
    lateinit var intent: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainActivityViewModel::class.java]
        setContentView(binding.root)
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
                    CoroutineScope(Dispatchers.IO).launch {
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
        init()
        checkFirstRun()
        Log.d("TEST_WORKER", "mode main activity: ${viewModel.mode}")
        if (viewModel.mode == "" || viewModel.mode == MODE_MY_BOOK) {
            viewMyBook()
        } else {
            viewNetBook()
        }
        setupOnClickListeners()
//        ContextCompat.startForegroundService(
//            this,
//            TorrentService.newIntent(this)
//        )
    }

    private fun setupOnClickListeners() {
        if (viewModel.mode == "" || viewModel.mode == MODE_MY_BOOK) {
            binding.btnAdd.setOnClickListener {
                if (binding.fragmentContainer == null) {
                    val intent = AddBookActivity.newIntentAddBook(this)
                    startActivity(intent)
                } else {
                    launchFragment(AddBookItemFragment.newInstanceAddItem())
                }
            }
        }
        binding.navView.setNavigationItemSelectedListener {
            when (it.title) {
                getString(R.string.general_book) -> {
                    viewModel.mode = MODE_NET_BOOK
                    viewNetBook()
                }
                getString(R.string.my_book) -> {
                    viewModel.mode = MODE_MY_BOOK
                    viewMyBook()
                }
            }
            true
        }
    }

    private fun viewMyBook() {
        bookListAdapterNet = null
        binding.drawlerLayout.closeDrawers()
        binding.navView.menu[0].icon = getDrawable(R.drawable.baseline_chevron_right_24)
        binding.navView.menu[1].icon = null
        binding.btnAdd.visibility = View.VISIBLE
        if (bookListAdapter == null){
            setupRecyclerView()
        }
        with(binding.mainRecycler) {
            adapter = bookListAdapter
        }
        viewModel.bookList.observe(this) {
            bookListAdapter?.submitList(it)
        }
        setupClickListener()
        setupSwipeListener(binding.mainRecycler)
    }

    private fun viewNetBook() {
        bookListAdapter = null
        binding.drawlerLayout.closeDrawers()
        binding.navView.menu[0].icon = null
        binding.navView.menu[1].icon = getDrawable(R.drawable.baseline_chevron_right_24)
        itemTouchHelper?.attachToRecyclerView(null)
        binding.btnAdd.visibility = View.GONE
        if (bookListAdapterNet == null) {
            bookListAdapterNet = BookNetworkAdapter()
        }
        with(binding.mainRecycler) {
            bookListAdapterNet = BookNetworkAdapter()
            adapter = bookListAdapterNet
        }
        viewModel.getListBooks()
        viewModel.bookListNet.observe(this) { bookItem ->
            bookListAdapterNet!!.submitList(bookItem)
        }
        setupClickListener()
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

    private fun init() {
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawlerLayout,
            binding.included.toolbar,
            R.string.button_open,
            R.string.button_closed
        )
        binding.drawlerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupRecyclerView() {
        if (viewModel.mode == "" || viewModel.mode == MODE_MY_BOOK) {
            with(binding.mainRecycler) {
                bookListAdapter = BookListAdapter()
                adapter = bookListAdapter
            }
        }
        if (viewModel.mode == MODE_NET_BOOK) {
            with(binding.mainRecycler) {
                bookListAdapterNet = BookNetworkAdapter()
                adapter = adapter
            }
        }
        setupClickListener()
        setupSwipeListener(binding.mainRecycler)
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
                    return makeMovementFlags(dragFlags,dragFlags)
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
        if (viewModel.mode == MODE_MY_BOOK) {
            bookListAdapter?.onEditBookClickListener = {
                if (binding.fragmentContainer == null) {
                    val intent = AddBookActivity.newIntentEditBook(this, it.id)
                    startActivity(intent)
                } else {
                    launchFragment(AddBookItemFragment.newInstanceEditItem(it.id))
                }
            }
            bookListAdapter?.onBookItemClickListener = {
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
            }
            RequestFileAccess.requestFileAccessPermission(this, {
                bookListAdapter?.loadCoverFunction = { holder, itemBook ->
                    val scope = CoroutineScope(Dispatchers.Main)
                    scope.launch {
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
                            ),
                            150,
                            150
                        )
                        val bindingCover = holder.binding as BookItemBinding
                        bindingCover.imCover.setImageBitmap(bitmap)
                    }
                }
            }) {}
        }
        if (viewModel.mode == MODE_NET_BOOK) {
            bookListAdapterNet?.onBookItemClickListener = {
                Log.d("TEST_WORKER", "on MA book item dto: ${it == null}")
                Log.d("TEST_WORKER", "on MA book item dto: ${it.title}")
                if (binding.fragmentContainer == null) // check landscape orientation
                {
                    val intent = BookInfoActivity.newIntentOpenItem(
                        this,
                        BookItem.UNDEFINED_ID,
                        BookInfoActivity.MODE_NET_BOOK,
                        it
                    )
                    startActivity(intent)
                } else {
                    launchFragment(
                        BookInfoFragment.newInstanceOpenItem(
                            BookItem.UNDEFINED_ID,
                            BookInfoActivity.MODE_NET_BOOK,
                            it
                        )
                    )
                }
            }
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