package com.skrash.book.presentation.openBookActivity.fb2Activity

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.skrash.book.BookApplication
import com.skrash.book.FormatBook.FB2
import com.skrash.book.FormatBook.FB2Parser.FictionBook
import com.skrash.book.R
import com.skrash.book.databinding.ActivityOpenFb2BookBinding
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.presentation.RequestFileAccess
import com.skrash.book.presentation.ViewModelFactory
import com.skrash.book.presentation.openBookActivity.pdfActivity.OpenBookActivity
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

        viewModel.fb2.observe(this){
            addAllElementsBook(it.fb2!!)
        }
    }

    private fun addAllElementsBook(fb: FictionBook){
        for (i in viewModel.fb2.value!!.listText){
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