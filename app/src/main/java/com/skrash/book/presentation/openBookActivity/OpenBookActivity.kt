package com.skrash.book.presentation.openBookActivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.skrash.book.R
import com.skrash.book.databinding.ActivityOpenBookBinding
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.presentation.BookApplication
import com.skrash.book.presentation.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
        viewModel.init(bookItemId, height)
        adapterInit(width, height)
    }
//        binding.btnClose.setOnClickListener {
//            viewModel.closeRender()
//        }

    @SuppressLint("SetTextI18n")
    private fun adapterInit(width: Int, height: Int) {
        with(binding.rvMain){
            pageAdapter = PageAdapter(viewModel, width, height)
            adapter = pageAdapter
        }
        viewModel.pageList.observe(this){
            pageAdapter.submitList(it)
        }
        viewModel.page.observe(this){
            binding.fabPageNum.text = it + " " + getString(R.string.page)
        }
        setupAdapterListener()
    }

    private fun setupAdapterListener(){
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