package com.skrash.book.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.skrash.book.R
import com.skrash.book.databinding.ActivityMainBinding
import com.skrash.book.presentation.bookInfoActivity.BookInfoActivity
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bookListAdapter: BookListAdapter
    private lateinit var viewModel: MainActivityViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (application as BookApplication).component
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        viewModel = ViewModelProvider(this, viewModelFactory)[MainActivityViewModel::class.java]
        viewModel.bookList.observe(this){
            bookListAdapter.submitList(it)
        }

    }

    private fun setupRecyclerView(){
        with(binding.mainRecycler){
            bookListAdapter = BookListAdapter()
            adapter = bookListAdapter
        }
        setupClickListener()
    }

    private fun setupClickListener(){
        bookListAdapter.onBookItemClickListener = {
            val intent = BookInfoActivity.newIntentOpenItem(this, it.id)
            startActivity(intent)
        }
    }
}