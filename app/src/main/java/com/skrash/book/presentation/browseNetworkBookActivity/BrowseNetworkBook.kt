package com.skrash.book.presentation.browseNetworkBookActivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.skrash.book.BookApplication
import com.skrash.book.R
import com.skrash.book.databinding.ActivityBrowseNetworkBookBinding
import com.skrash.book.presentation.ViewModelFactory
import com.skrash.book.presentation.mainAcitivity.MainActivity
import javax.inject.Inject

class BrowseNetworkBook : AppCompatActivity() {

    private lateinit var binding: ActivityBrowseNetworkBookBinding
    private lateinit var viewModel: BrowseNetworkBookViewModel
    private lateinit var bookListAdapter: BookNetworkAdapter
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (application as BookApplication).component
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)

        super.onCreate(savedInstanceState)
        binding = ActivityBrowseNetworkBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavMenu()

        viewModel = ViewModelProvider(this, viewModelFactory)[BrowseNetworkBookViewModel::class.java]
        setupRecyclerView()
        viewModel.getListBooks()
        viewModel.bookList.observe(this) {
            bookListAdapter.submitList(it)
        }
    }

    private fun initNavMenu() {
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawlerLayout,
            binding.included.toolbar,
            R.string.button_open,
            R.string.button_closed
        )
        binding.drawlerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener {
            when (it.title){
                getString(R.string.my_book) -> {
                    val intent = MainActivity.newIntent(this)
                    startActivity(intent)
                }
            }
            true
        }
    }

    private fun setupRecyclerView() {
        with(binding.mainRecycler) {
            bookListAdapter = BookNetworkAdapter()
            adapter = bookListAdapter
        }
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, BrowseNetworkBook::class.java)
        }
    }
}