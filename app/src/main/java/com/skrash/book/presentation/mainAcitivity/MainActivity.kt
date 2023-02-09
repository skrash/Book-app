package com.skrash.book.presentation.mainAcitivity

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.skrash.book.R
import com.skrash.book.databinding.ActivityMainBinding
import com.skrash.book.databinding.BookItemBinding
import com.skrash.book.domain.entities.FormatBook
import com.skrash.book.presentation.BookApplication
import com.skrash.book.presentation.ViewModelFactory
import com.skrash.book.presentation.addBookActivity.AddBookActivity
import com.skrash.book.presentation.addBookActivity.AddBookItemFragment
import com.skrash.book.presentation.bookInfoActivity.BookInfoActivity
import com.skrash.book.presentation.bookInfoActivity.BookInfoFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class MainActivity : AppCompatActivity(), AddBookItemFragment.OnEditingFinishedListener {

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
        init()
        setupRecyclerView()
        viewModel = ViewModelProvider(this, viewModelFactory)[MainActivityViewModel::class.java]
        viewModel.bookList.observe(this){
            bookListAdapter.submitList(it)
        }
        binding.btnAdd.setOnClickListener {
            if (binding.fragmentContainer == null){
                val intent = AddBookActivity.newIntentAddBook(this)
                startActivity(intent)
            } else {
                launchFragment(AddBookItemFragment.newInstanceAddItem())
            }
        }
    }

    private fun init(){
        val toggle = ActionBarDrawerToggle(this, binding.drawlerLayout, binding.included.toolbar, R.string.button_open, R.string.button_closed)
        binding.drawlerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupRecyclerView(){
        with(binding.mainRecycler){
            bookListAdapter = BookListAdapter()
            adapter = bookListAdapter
        }
        setupClickListener()
        setupSwipeListener(binding.mainRecycler)
    }

    private fun setupSwipeListener(rvShopList: RecyclerView) {
        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = bookListAdapter.currentList[viewHolder.adapterPosition]
                viewModel.deleteShopItem(item)
            }
        }
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(rvShopList)
    }

    private fun setupClickListener(){
        bookListAdapter.onEditBookClickListener = {
            if (binding.fragmentContainer == null){
                val intent = AddBookActivity.newIntentEditBook(this, it.id)
                startActivity(intent)
            } else {
                launchFragment(AddBookItemFragment.newInstanceEditItem(it.id))
            }
        }
        bookListAdapter.onBookItemClickListener = {
            if (binding.fragmentContainer == null) // check landscape orientation
            {
                val intent = BookInfoActivity.newIntentOpenItem(this, it.id)
                startActivity(intent)
            }else{
                launchFragment(BookInfoFragment.newInstanceOpenItem(it.id))
            }
        }
        bookListAdapter.loadCoverFunction = {
            holder, itemBook ->
            val scope = CoroutineScope(Dispatchers.Main)
            scope.launch {
                val bitmap = viewModel.getBookCover(FormatBook.valueOf(itemBook.fileExtension.uppercase()), itemBook.path, 150, 150)
                val bindingCover = holder.binding as BookItemBinding
                bindingCover.imCover.setImageBitmap(bitmap)
            }
        }
    }

    private fun launchFragment(fragment: Fragment){
        supportFragmentManager.popBackStack()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onEditingFinishedListener() {
        supportFragmentManager.popBackStack()
    }
}