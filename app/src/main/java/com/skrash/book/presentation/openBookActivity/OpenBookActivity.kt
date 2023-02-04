package com.skrash.book.presentation.openBookActivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.skrash.book.databinding.ActivityOpenBookBinding
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.presentation.BookApplication
import com.skrash.book.presentation.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)

        super.onCreate(savedInstanceState)
        binding = ActivityOpenBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        parseIntent()
        viewModel = ViewModelProvider(this, viewModelFactory)[OpenBookViewModel::class.java]
        binding.imageView.viewTreeObserver.addOnGlobalLayoutListener {
            viewModel.init(bookItemId, binding.imageView.width, binding.imageView.height)
            viewModel.bitmap.observe(this){
                binding.imageView.setImageBitmap(it)
            }
        }
//        binding.btnClose.setOnClickListener {
//            viewModel.closeRender()
//        }
    }

    private fun parseIntent(){
        if (!intent.hasExtra(BOOK_ITEM_ID)){
            throw RuntimeException("Param book item id is absent")
        }
        bookItemId = intent.getIntExtra(BOOK_ITEM_ID, BookItem.UNDEFINED_ID)
        Log.d("TEST", bookItemId.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.closeRender()
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