package com.skrash.book.presentation.openBookActivity.fb2Activity

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.skrash.book.BookApplication
import com.skrash.book.FormatBook.FB2
import com.skrash.book.FormatBook.FB2Parser.FictionBook
import com.skrash.book.R
import com.skrash.book.databinding.ActivityOpenFb2BookBinding
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.Bookmark
import com.skrash.book.presentation.RequestFileAccess
import com.skrash.book.presentation.ViewModelFactory
import kotlinx.coroutines.*
import javax.inject.Inject


class OpenFB2BookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOpenFb2BookBinding
    private var bookItemId = BookItem.UNDEFINED_ID
    private lateinit var viewModel: OpenFB2BookViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val component by lazy {
        (application as BookApplication).component
    }

    private lateinit var bookmarkMenu: SubMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)

        binding = ActivityOpenFb2BookBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        parseIntent()

        viewModel = ViewModelProvider(this, viewModelFactory)[OpenFB2BookViewModel::class.java]
        RequestFileAccess.requestFileAccessPermission(this, {
            viewModel.init(bookItemId)
        }) {
            Toast.makeText(
                this,
                getString(R.string.permission_file_access_denied),
                Toast.LENGTH_LONG
            ).show()
        }

        viewModel.fb2.observe(this) {
            addAllElementsBook(it.fb2!!)
        }

        setupListeners()

        //  Menu Bookmark create
        bookmarkMenu = binding.navBookmark.menu.addSubMenu("${getString(R.string.bookmark)}")
        //

        viewModel.bookmarkList.observe(this) {
            addBookmarkToNavMenu(it)
            for (i in it) {
                bookmarkSetImg(viewModel.offset.value!!.toInt() == i.page)
            }
        }

        binding.scroll.viewTreeObserver.addOnScrollChangedListener {
            viewModel.setPage(binding.scroll.scrollY)
        }

        viewModel.offset.observe(this) {
            binding.fabPageNum.text = it
            bookmarkSetImg(isPageHaveBookmark(it.toInt()))
        }
    }

    private fun setupListeners() {
        binding.fabPageNum.setOnClickListener {
            binding.fabPageNum.visibility = View.GONE
            binding.tiPage.visibility = View.VISIBLE
            binding.tiPage.setOnEditorActionListener { textView, i, event ->
                if (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER) || (i == EditorInfo.IME_ACTION_DONE)) {
                    binding.tiPage.visibility = View.GONE
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.tiPage.windowToken, 0)
                    goToPage(binding.tiPage.text.toString().toInt())
                }
                false
            }
            binding.tiPage.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.tiPage, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.imBookmark.setOnClickListener {
            if (viewModel.offset.value != null) {
                if (isPageHaveBookmark(viewModel.offset.value!!.toInt())) {
                    bookmarkSetImg(false)
                    viewModel.deleteBookmark(viewModel.offset.value!!.toInt())
                } else {
                    viewModel.addBookmark(viewModel.offset.value!!.toInt())
                }
            }
        }
        binding.navBookmark.setNavigationItemSelectedListener {
            goToPage(it.itemId)
            binding.flRoot.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun goToPage(page: Int) {
        viewModel.jumpTo(page)
        binding.scroll.scrollTo(0, page)
    }

    private fun saveCurrentPage(page: Int) {
        viewModel.finish(page)
    }

    override fun onDestroy() {
        saveCurrentPage(viewModel.offset.value.toString().toInt())
        super.onDestroy()
    }

    override fun onBackPressed() {
        saveCurrentPage(viewModel.offset.value.toString().toInt())
        super.onBackPressed()
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

    private fun bookmarkSetImg(active: Boolean) {
        if (active) {
            binding.imBookmark.setImageResource(R.mipmap.ic_bookmark_colored_foreground)
            binding.imBookmark.alpha = 1.0f
            CoroutineScope(Dispatchers.Main).launch {
                delay(1500)
                binding.imBookmark.alpha = 0.1f
            }
        } else {
            binding.imBookmark.setImageResource(R.drawable.bookmark)
            binding.imBookmark.alpha = 0.1f
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

    private fun addAllElementsBook(fb: FictionBook) {
        for (i in viewModel.fb2.value!!.listText) {
            if (i.contains(FB2.IMAGE_TAG)) {
                val valueImage = i.replace(FB2.IMAGE_TAG, "")
                val strImgValue = fb.binaries.getValue(valueImage)
                if (strImgValue.contentType == "image/jpeg" || strImgValue.contentType == "image/png") {
                    val imageBytes = Base64.decode(strImgValue.binary, Base64.DEFAULT)
                    val decodedImage =
                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    val image = ImageView(this)
                    image.setImageBitmap(decodedImage)
                    binding.llRoot.addView(image)
                    val space = Space(this)
                    binding.llRoot.addView(space)
                    space.layoutParams.height = 16
                }
            } else {
                val textView = TextView(this)
                var kk = i
                if (i.contains(FB2.BOLD_TEXT)) {
                    kk = i.replace(FB2.BOLD_TEXT, "")
                    textView.setTypeface(null, Typeface.BOLD)
                }
                textView.text = kk
                binding.llRoot.addView(textView)
                val space = Space(this)
                binding.llRoot.addView(space)
                space.layoutParams.height = 16
            }
        }
        listenIfStartNotNullPage()
    }

    private fun listenIfStartNotNullPage(){
        // if start not 0 page
        binding.rootFrame.removeView(binding.progressBar)

        CoroutineScope(Dispatchers.Default).launch {
            delay(1000)
            withContext(Dispatchers.Main){
                viewModel.bookItem.observe(this@OpenFB2BookActivity) {
                    if (it.startOnPage != 0) {
                        goToPage(it.startOnPage)
                    }
                }
            }
        }
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
            val intent = Intent(context, OpenFB2BookActivity::class.java)
            intent.putExtra(BOOK_ITEM_ID, bookItemId)
            return intent
        }
    }
}