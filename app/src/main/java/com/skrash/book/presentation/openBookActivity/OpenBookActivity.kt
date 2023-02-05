package com.skrash.book.presentation.openBookActivity

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.skrash.book.databinding.ActivityOpenBookBinding
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.presentation.BookApplication
import com.skrash.book.presentation.ViewModelFactory
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

    private lateinit var pageAdapter: PageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)

        super.onCreate(savedInstanceState)
        binding = ActivityOpenBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        parseIntent()

        viewModel = ViewModelProvider(this, viewModelFactory)[OpenBookViewModel::class.java]
        viewModel.init(bookItemId)
        adapterInit()
    }
//        binding.btnClose.setOnClickListener {
//            viewModel.closeRender()
//        }

    private fun adapterInit() {
        with(binding.rvMain){
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val width = size.x
            val height = size.y
            pageAdapter = PageAdapter(viewModel, width, height)
            adapter = pageAdapter
        }
        viewModel.pageList.observe(this){
            pageAdapter.submitList(it)
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
            val intent = Intent(context, OpenBookActivity::class.java)
            intent.putExtra(BOOK_ITEM_ID, bookItemId)
            return intent
        }
    }
}