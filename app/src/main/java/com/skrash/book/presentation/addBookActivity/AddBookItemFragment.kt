package com.skrash.book.presentation.addBookActivity

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.viewmodel.viewModelFactory
import com.skrash.book.R
import com.skrash.book.databinding.FragmentAddBookItemBinding
import com.skrash.book.domain.entities.BookItem
import com.skrash.book.domain.entities.FormatBook
import com.skrash.book.domain.entities.Genres
import com.skrash.book.presentation.BookApplication
import com.skrash.book.presentation.ViewModelFactory
import javax.inject.Inject
import kotlin.concurrent.thread

class AddBookItemFragment : Fragment() {

    private var _binding: FragmentAddBookItemBinding? = null
    private val binding: FragmentAddBookItemBinding
        get() = _binding ?: throw RuntimeException("FragmentAddBookItemBinding == null")

    private var screenMode: String = UNKNOWN_MODE
    private var bookItemId: Int = BookItem.UNDEFINED_ID

    private lateinit var viewModel: AddBookItemViewModel
    private lateinit var onEditingFinishedListener: OnEditingFinishedListener

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as BookApplication).component
    }

    override fun onAttach(context: Context) {
        component.inject(this)

        super.onAttach(context)

        if (context is OnEditingFinishedListener) {
            onEditingFinishedListener = context
        } else {
            throw RuntimeException("Activity must implement OnEditingFinishedListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseParams()
    }

    private fun parseParams() {
        val args = requireArguments()
        if (!args.containsKey(SCREEN_MODE)) {
            throw RuntimeException("Param screen mode is absent")
        }
        val mode = args.getString(SCREEN_MODE)
        if (mode != MODE_EDIT && mode != MODE_ADD) {
            throw RuntimeException("Unknown screen mode $mode")
        }
        screenMode = mode
        if (screenMode == MODE_EDIT) {
            if (!args.containsKey(BOOK_ITEM_ID)) {
                throw RuntimeException("Param shop item id is absent")
            }
            bookItemId = args.getInt(BOOK_ITEM_ID, BookItem.UNDEFINED_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddBookItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)[AddBookItemViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        addTextChangeListeners()
        launchRightMode()
        observeViewModel()
    }

    private fun addTextChangeListeners() {
        binding.tiTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.resetErrorInputTitle()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.tiAuthor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.resetErrorInputAuthor()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.tiDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.resetErrorInputDescription()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.tiGenres.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.resetErrorInputGenres()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.tiTags.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.resetErrorInputTags()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.tiPath.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.resetErrorInputPath()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun observeViewModel() {
        viewModel.shouldCloseScreen.observe(viewLifecycleOwner){
            onEditingFinishedListener.onEditingFinishedListener()
        }
    }

    private fun launchRightMode() {
        when (screenMode) {
            MODE_EDIT -> launchEditMode()
            MODE_ADD -> launchAddMode()
        }
    }

    private fun launchEditMode(){
        viewModel.getBookItem(bookItemId)
        binding.btnCancel.setOnClickListener {
            viewModel.finishWork()
        }
        binding.btnSave.setOnClickListener {
            viewModel.finishEditing(
                binding.tiTitle.text?.toString(),
                binding.tiAuthor.text?.toString(),
                binding.tiDescription.text?.toString(),
                binding.tiGenres.text?.toString(),
                binding.tiTags.text?.toString(),
                binding.tiPath.text?.toString()
            )
        }
    }

    private fun launchAddMode(){
        binding.btnCancel.setOnClickListener {
            viewModel.finishWork()
        }
        binding.btnSave.setOnClickListener {
            viewModel.finishEditing(
                binding.tiTitle.text?.toString(),
                binding.tiAuthor.text?.toString(),
                binding.tiDescription.text?.toString(),
                binding.tiGenres.text?.toString(),
                binding.tiTags.text?.toString(),
                binding.tiPath.text?.toString()
            )
        }
    }

    companion object {

        private const val SCREEN_MODE = "screen_mode"
        private const val MODE_ADD = "mode_add"
        private const val MODE_EDIT = "mode_edit"
        private const val UNKNOWN_MODE = ""
        private const val BOOK_ITEM_ID = "book_item_id"

        fun newInstanceAddItem(): AddBookItemFragment {
            return AddBookItemFragment().apply {
                arguments = Bundle().apply {
                    putString(SCREEN_MODE, MODE_ADD)
                }
            }
        }

        fun newInstanceEditItem(bookItemId: Int): AddBookItemFragment {
            return AddBookItemFragment().apply {
                arguments = Bundle().apply {
                    putString(SCREEN_MODE, MODE_EDIT)
                    putInt(BOOK_ITEM_ID, bookItemId)
                }
            }
        }
    }

    interface OnEditingFinishedListener {

        fun onEditingFinishedListener()
    }

}