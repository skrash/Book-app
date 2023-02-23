package com.skrash.book.presentation.openBookActivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt


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

    private lateinit var pageLayoutManager: PageGridLayoutManager

    private val offsetXimg = MutableLiveData<Int>()
    private val offsetYimg = MutableLiveData<Int>()
    private val scale = MutableLiveData<Float>()

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
        RequestFileAccess.requestFileAccessPermission(this, {
            viewModel.init(bookItemId, height)
        }) {
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
                bookmarkSetImg(viewModel.page.value!!.toInt() == i.page)
            }
        }
        viewModel.page.observe(this) {
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
                if (isPageHaveBookmark(viewModel.page.value!!.toInt())) {
                    bookmarkSetImg(false)
                    viewModel.deleteBookmark(viewModel.page.value!!.toInt())
                } else {
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

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun adapterInit(width: Int, height: Int) {
        pageLayoutManager = PageGridLayoutManager(this@OpenBookActivity)
        with(binding.rvMain) {
            pageAdapter = PageAdapter()
            layoutManager = pageLayoutManager
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
            if (it.startOnPage != 0) {
                goToPage(it.startOnPage)
            }
        }
        val gestureMoveDetector =
            GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onScroll(
                    e1: MotionEvent,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    if (e1.x - e2.x > 120 && abs(distanceX) > 20) {
                        offsetXimg.value = (((e1.x - e2.x) * -1).roundToInt())
                        return false
                    }
                    if (e1.y - e2.y > 120 && abs(distanceY) > 20) {
                        offsetYimg.value = (((e1.y - e2.y) * -1).roundToInt())
                        return false
                    }
                    if (e2.y - e1.y > 120 && abs(distanceY) > 20) {
                        offsetYimg.value = ((e2.y - e1.y).roundToInt())
                        return false
                    }
                    if (e2.x - e1.x > 120 && Math.abs(distanceX) > 20) {
                        offsetXimg.value = ((e2.x - e1.x).roundToInt())
                        return false
                    }
                    return true
                }
            })
        val gestureDoubleTouchDetector =
            GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    return true
                }
            })
        val gesturePinch =
            ScaleGestureDetector(
                this,
                object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        scale.value = detector.scaleFactor
                        return false
                    }
                })
        pageAdapter.renderPageImage = { holder, position ->
            val bitmap = viewModel.getPage(position, width, height)
            val holderBinding = holder.binding as PageItemBinding
            holderBinding.ivMain.setImageBitmap(bitmap)
            if (position == viewModel.page.value?.toInt()) {
                scale.observe(this) {
                    if (it < 1f) {
                        if (holder.binding.ivMain.scaleY - it > 1f) {
                            holder.binding.ivMain.scaleY -= 1f - it
                            holder.binding.ivMain.scaleX -= 1f - it
                            pageLayoutManager.setScrollEnabled(false)
                        }
                    } else {
                        if (holder.binding.ivMain.scaleY + it < 5f) {
                            holder.binding.ivMain.scaleY += it - 1f
                            holder.binding.ivMain.scaleX += it - 1f
                            pageLayoutManager.setScrollEnabled(false)
                        }
                    }
                    if (it == 1f) {
                        holder.binding.ivMain.scaleY = 1f
                        holder.binding.ivMain.scaleX = 1f
                        pageLayoutManager.setScrollEnabled(true)
                    }
                }
                offsetXimg.observe(this) {
                    if (!pageLayoutManager.getScrollEnabled() &&
                        holder.binding.ivMain.x + it < ((holder.binding.ivMain.width * holder.binding.ivMain.scaleX) - holder.binding.ivMain.width) / 2 && // right limiter
                        holder.binding.ivMain.x + it > -(((holder.binding.ivMain.width * holder.binding.ivMain.scaleX) - holder.binding.ivMain.width) / 2) // left limiter
                    ) {
                        holder.binding.ivMain.x += it
                    }
                }
                offsetYimg.observe(this) {
                    if (!pageLayoutManager.getScrollEnabled() &&
                        holder.binding.ivMain.y + it < ((holder.binding.ivMain.height * holder.binding.ivMain.scaleY) - holder.binding.ivMain.height) / 2 && // right limiter
                        holder.binding.ivMain.y + it > -(((holder.binding.ivMain.height * holder.binding.ivMain.scaleY) - holder.binding.ivMain.height) / 2) // left limiter
                    ) {
                        holder.binding.ivMain.y += it
                    }
                }
                holderBinding.clRoot.setOnTouchListener { view, motionEvent ->
                    var eventDoubleTouch = gestureDoubleTouchDetector.onTouchEvent(motionEvent)
                    if (eventDoubleTouch) {
                        if (holder.binding.ivMain.scaleX > 1f) {
                            holder.binding.ivMain.x = 0f
                            holder.binding.ivMain.y = 0f
                            offsetXimg.value = 0
                            offsetYimg.value = 0
                            scale.value = 1f
                            pageLayoutManager.setScrollEnabled(true)
                        } else {
                            pageLayoutManager.setScrollEnabled(false)
                            goToPage(position)
                            scale.value = 2f
                        }
                        return@setOnTouchListener true
                    }
                    if (motionEvent.pointerCount == 1) {
                        val move = gestureMoveDetector.onTouchEvent(motionEvent)
                        if (move) {
                            return@setOnTouchListener true
                        }
                    }
                    if (motionEvent.pointerCount == 2) {
                        gesturePinch.onTouchEvent(motionEvent)
                    }
                    return@setOnTouchListener true
                }
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