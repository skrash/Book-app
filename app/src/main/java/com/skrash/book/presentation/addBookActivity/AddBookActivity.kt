package com.skrash.book.presentation.addBookActivity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.skrash.book.R
import com.skrash.book.domain.entities.BookItem

class AddBookActivity : AppCompatActivity(), AddBookItemFragment.OnEditingFinishedListener {

    private var screenMode = UNKNOWN_MODE
    private var bookItemId = BookItem.UNDEFINED_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)
        parseIntent()
        if (savedInstanceState == null) {
            launchRightMode()
        }
    }

    private fun parseIntent(){
        if (!intent.hasExtra(SCREEN_MODE)) {
            throw RuntimeException("Param screen mode is absent")
        }
        val mode = intent.getStringExtra(SCREEN_MODE)
        if (mode != MODE_EDIT && mode != MODE_ADD) {
            throw RuntimeException("Unknown screen mode $mode")
        }
        screenMode = mode
        if (screenMode == MODE_EDIT) {
            if (!intent.hasExtra(BOOK_ITEM_ID)) {
                throw RuntimeException("Param book item id is absent")
            }
            bookItemId = intent.getIntExtra(BOOK_ITEM_ID, BookItem.UNDEFINED_ID)
        }
    }

    private fun launchRightMode() {
        val fragment = when (screenMode) {
            MODE_EDIT -> AddBookItemFragment.newInstanceEditItem(bookItemId)
            MODE_ADD  -> AddBookItemFragment.newInstanceAddItem()
            else      -> throw RuntimeException("Unknown screen mode $screenMode")
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.add_book_item_container, fragment)
            .commit()
    }

    companion object {

        private const val SCREEN_MODE = "screen_mode"
        private const val MODE_ADD = "mode_add"
        private const val MODE_EDIT = "mode_edit"
        private const val UNKNOWN_MODE = ""
        private const val BOOK_ITEM_ID = "book_item_id"

        fun newIntentAddBook(context: Context): Intent{
            val intent = Intent(context, AddBookActivity::class.java)
            intent.putExtra(SCREEN_MODE, MODE_ADD)
            return intent
        }

        fun newIntentEditBook(context: Context, bookItemId: Int): Intent {
            val intent = Intent(context, AddBookActivity::class.java)
            intent.putExtra(SCREEN_MODE, MODE_EDIT)
            intent.putExtra(BOOK_ITEM_ID, bookItemId)
            return intent
        }
    }

    override fun onEditingFinishedListener() {
        finish()
    }
}