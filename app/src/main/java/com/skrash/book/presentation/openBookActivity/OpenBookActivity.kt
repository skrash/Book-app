package com.skrash.book.presentation.openBookActivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.core.view.GravityCompat
import androidx.core.view.iterator
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.skrash.book.R
import com.skrash.book.databinding.ActivityOpenBookBinding
import com.skrash.book.databinding.PageItemBinding
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.Bookmark
import com.skrash.book.presentation.BookApplication
import com.skrash.book.presentation.RequestFileAccess
import com.skrash.book.presentation.ViewModelFactory
import kotlinx.coroutines.*
import javax.inject.Inject


class OpenBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOpenBookBinding
    private var bookItemId = BookItem.UNDEFINED_ID
    private lateinit var viewModel: OpenBookViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val component by lazy {
        (application as BookApplication).component
    }

    val coroutine = CoroutineScope(Dispatchers.Main)

    private lateinit var pageAdapter: PageAdapter
    private lateinit var bookmarkMenu: SubMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)

        super.onCreate(savedInstanceState)
        binding = ActivityOpenBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        parseIntent()
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val height = size.y
        val width = size.x
        viewModel = ViewModelProvider(this, viewModelFactory)[OpenBookViewModel::class.java]
        if (RequestFileAccess.isStoragePermissionGranted(this)) {
            viewModel.init(bookItemId, height)
        } else {
            Toast.makeText(
                this,
                getString(R.string.permission_file_access_denied),
                Toast.LENGTH_LONG
            ).show()
        }
        adapterInit(width, height)
        setupListeners()
        //  Menu Bookmark create
        bookmarkMenu = binding.navBookmark.menu.addSubMenu("${getString(R.string.bookmark)}")
        //
        viewModel.bookmarkList.observe(this) {
            addBookmarkToNavMenu(it)
            for (i in it) {
                Log.d("TEST8", "observed page: ${i.page.toString()}")
                bookmarkSetImg(viewModel.page.value!!.toInt() == i.page)
            }
        }
        viewModel.page.observe(this) {
            Log.d("TEST11", "curr page $it")
            bookmarkSetImg(isPageHaveBookmark(it.toInt()))
        }
    }

    private fun addBookmarkToNavMenu(listBookmark: List<Bookmark>) {
        bookmarkMenu.clear()
        for (i in listBookmark) {
            bookmarkMenu.add(
                bookmarkMenu.item.itemId,
                i.page,
                Menu.NONE,
                "${i.page} ${getString(R.string.page)}"
            )
        }
    }

    private fun bookmarkSetImg(active: Boolean) {
        if (active) {
            binding.imBookmark.setImageResource(R.mipmap.ic_bookmark_colored_foreground)
            binding.imBookmark.alpha = 1.0f
            coroutine.launch {
                delay(1500)
                binding.imBookmark.alpha = 0.1f
            }
        } else {
            binding.imBookmark.setImageResource(R.drawable.bookmark)
            binding.imBookmark.alpha = 0.1f
        }
    }

    private fun isPageHaveBookmark(page: Int): Boolean {
        if (viewModel.bookmarkList.value != null) {
            for (i in viewModel.bookmarkList.value!!) {
                if (i.page == page) {
                    return true
                }
            }
        }
        return false
    }

    private fun setupListeners() {
        binding.fabPageNum.setOnClickListener {
            binding.fabPageNum.visibility = View.GONE
            binding.editText.visibility = View.VISIBLE
            binding.editText.setOnEditorActionListener { textView, i, event ->
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) || (i == EditorInfo.IME_ACTION_DONE)) {
                    binding.editText.visibility = View.GONE
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.editText.windowToken, 0)
                    Log.d(
                        "TEST6",
                        (binding.editText.text.toString().toInt() * viewModel.height).toString()
                    )
                    goToPage(binding.editText.text.toString().toInt())
                }
                false
            }
            binding.editText.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.editText, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.imBookmark.setOnClickListener {
            if (viewModel.page.value != null) {
                Log.d("TEST12", "imBook clicked, page: ${viewModel.page.value!!.toInt()}")
                if (isPageHaveBookmark(viewModel.page.value!!.toInt())) {
                    bookmarkSetImg(false)
                    Log.d("TEST12", "mode delete")
                    viewModel.deleteBookmark(viewModel.page.value!!.toInt())
                } else {
                    Log.d("TEST12", "mode add")
                    viewModel.addBookmark(viewModel.page.value!!.toInt())
                }
            }
        }
        binding.navBookmark.setNavigationItemSelectedListener {
            goToPage(it.itemId)
            binding.flRoot.closeDrawer(GravityCompat.START)
            true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun adapterInit(width: Int, height: Int) {
        with(binding.rvMain) {
            pageAdapter = PageAdapter()
            adapter = pageAdapter
        }
        viewModel.pageList.observe(this) {
            pageAdapter.submitList(it)
        }
        viewModel.page.observe(this) {
            binding.fabPageNum.text = it + " " + getString(R.string.page)
        }
        // if start not 0 page
        viewModel.bookItem.observe(this) {
            Log.d("TEST7", "start page: ${it.startOnPage.toString()}")
            if (it.startOnPage != 0) {
                goToPage(it.startOnPage)
            }
        }
        pageAdapter.renderPageImage = { holder, position ->
            if (viewModel.pdfRenderer != null) {
                val page = viewModel.pdfRenderer!!.openPage(position)
                val bitmap = Bitmap.createBitmap(
                    width, height,
                    Bitmap.Config.ARGB_4444
                )
                page!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                val holderBinding = holder.binding as PageItemBinding
                holderBinding.ivMain.setImageBitmap(bitmap)
                page.close()
            }
        }
        setupAdapterListener()
    }

    private fun setupAdapterListener() {
        binding.rvMain.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                viewModel.scrolling(dy)
                binding.fabPageNum.visibility = View.VISIBLE
                coroutine.launch {
                    delay(1500)
                    binding.fabPageNum.visibility = View.GONE
                }
                Log.d("TEST5", viewModel.page.value!!)
            }
        })
    }

    private fun goToPage(page: Int) {
        binding.rvMain.scrollToPosition(page)
        viewModel.jumpTo(page)
    }

    private fun saveCurrentPage(page: Int) {
        viewModel.finish(page)
    }

    override fun onDestroy() {
        saveCurrentPage(viewModel.page.value.toString().toInt())
        super.onDestroy()
    }

    override fun onBackPressed() {
        Log.d("TEST7", "BACK PRESSED")
        saveCurrentPage(viewModel.page.value.toString().toInt())
        super.onBackPressed()
    }

    private fun parseIntent() {
        if (!intent.hasExtra(BOOK_ITEM_ID)) {
            throw RuntimeException("Param book item id is absent")
        }
        bookItemId = intent.getIntExtra(BOOK_ITEM_ID, BookItem.UNDEFINED_ID)
    }

    companion object {

        private const val BOOK_ITEM_ID = "book_item_id"

        fun newIntent(context: Context, bookItemId: Int): Intent {
            val intent = Intent(context, OpenBookActivity::class.java)
            intent.putExtra(BOOK_ITEM_ID, bookItemId)
            return intent
        }
    }
}