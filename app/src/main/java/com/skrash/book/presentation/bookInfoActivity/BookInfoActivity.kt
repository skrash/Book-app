package com.skrash.book.presentation.bookInfoActivity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.skrash.book.R
import com.skrash.book.domain.entities.BookItem

class BookInfoActivity : AppCompatActivity() {

    private var bookItemId = BookItem.UNDEFINED_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_info)
        parseIntent()
        supportFragmentManager.beginTransaction()
            .replace(R.id.book_item_container, BookInfoFragment.newInstanceOpenItem(bookItemId))
            .commit()
    }

    private fun parseIntent(){
        if (!intent.hasExtra(BOOK_ITEM_ID)){
            throw RuntimeException("Param book item id is absent")
        }
        bookItemId = intent.getIntExtra(BOOK_ITEM_ID, BookItem.UNDEFINED_ID)
    }

    companion object {
        private const val BOOK_ITEM_ID = "book_item_id"

        fun newIntentOpenItem(context: Context, bookItemId: Int): Intent {
            val intent = Intent(context, BookInfoActivity::class.java)
            intent.putExtra(BOOK_ITEM_ID, bookItemId)
            return intent
        }
    }
}