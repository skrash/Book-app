package com.skrash.book.presentation.bookInfoActivity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.skrash.book.R
import com.skrash.book.data.network.model.BookItemDto
import com.skrash.book.domain.entities.BookItem

class BookInfoActivity : AppCompatActivity() {

    private var bookItemId = BookItem.UNDEFINED_ID
    lateinit var mode: String
    private var bookItemDto: BookItemDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_info)
        parseIntent()
        if (mode == MODE_MY_BOOK){
            supportFragmentManager.beginTransaction()
                .replace(R.id.book_item_container, BookInfoFragment.newInstanceOpenItem(bookItemId, mode))
                .commit()
        }
        if (mode == MODE_NET_BOOK){
            Log.d("TEST_WORKER", "${bookItemDto == null}")
            supportFragmentManager.beginTransaction()
                .replace(R.id.book_item_container, BookInfoFragment.newInstanceOpenItem(bookItemId, mode, bookItemDto))
                .commit()
        }
    }

    private fun parseIntent(){
        mode = intent.getStringExtra(MODE) ?: throw RuntimeException("Param mode is absent")
        Log.d("TEST_WORKER", "mode: $mode")
        if (mode != MODE_MY_BOOK && mode != MODE_NET_BOOK) {
            throw RuntimeException("Unknown screen mode $mode")
        }
        if (mode == MODE_MY_BOOK){
            bookItemId = intent.getIntExtra(BOOK_ITEM_ID, BookItem.UNDEFINED_ID)
        }
        if (mode == MODE_NET_BOOK){
            bookItemDto = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.extras?.getParcelable(BOOK_ITEM_DTO, BookItemDto::class.java)
            } else {
                intent.extras?.getParcelable<BookItemDto>(BOOK_ITEM_DTO)
            }
        }
    }

    companion object {
        private const val BOOK_ITEM_ID = "book_item_id"
        private const val BOOK_ITEM_DTO = "book_item_dto"

        private const val MODE = "mode"
        const val MODE_MY_BOOK = "my_book"
        const val MODE_NET_BOOK = "net_book"
        fun newIntentOpenItem(context: Context, bookItemId: Int, mode: String, bookItemDto: BookItemDto? = null): Intent {
            Log.d("TEST_WORKER", "in companion ${bookItemDto == null}")
            val intent = Intent(context, BookInfoActivity::class.java)
            if (mode == MODE_NET_BOOK){
                intent.apply {
                    putExtra(MODE, MODE_NET_BOOK)
                    putExtra(BOOK_ITEM_DTO, bookItemDto)
                }
            } else {
                intent.putExtra(MODE, MODE_MY_BOOK)
                intent.putExtra(BOOK_ITEM_ID, bookItemId)
            }
            return intent
        }
    }
}